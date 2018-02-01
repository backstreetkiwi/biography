package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.metadata.MetadataService;

/**
 * This service offers methods to validate archived media files.
 * 
 * The validating methods are ordered by the strength of the validation criteria. They call each other in a kind of
 * cascade.
 * 
 * Methods ordered by criteria strength:
 * 
 * <ul>
 * <li>{@link #hasMediaFileName(File)}</li>
 * <li>{@link #isInCorrectArchiveFolder(File)}</li>
 * <li>{@link #hasMetadata(File)}</li>
 * <li>{@link #doesMetadataDatetimeOriginalMatchFilename(File)}</li>
 * <li>{@link #doesMetadataMatchExifData(File)}</li>
 * </ul>
 * 
 * The method {@link #check(File)} performs all validations sequentially until the first one fails and provides a
 * protocol of the validations.
 * 
 * @author mail@nikolaus-winter.de
 *
 */
@Component
public class ArchiveValidationService {

    private final static Log LOG = LogFactory.getLog(ArchiveValidationService.class);

    private MetadataService metadataService;

    private File archiveFolder;

    public ArchiveValidationService(MetadataService metadataService, File archiveFolder) {
        this.metadataService = metadataService;
        this.archiveFolder = archiveFolder;
        LOG.info("ArchiveValidationService started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
    }

    /**
     * Is the archive file valid?
     * 
     * The check is performed by using just the method {@link #isHashcodeCorrect(File)} because that validation method
     * is the strongest in the cascade of validations.
     * 
     * Exceptions are swallowed and result in a false value because this method does not provide further information
     * about the reasons of a failure.
     * 
     * If you need further information, use {@link #check(File)}.
     * 
     * @param file
     *            archive file
     * @return Is the archive file valid?
     */
    public boolean isValid(File file) {
        try {
            return isHashcodeCorrect(file);
        } catch (Exception e) {
            return false;
        }
    }

    public Pair<Boolean, List<Pair<String, Boolean>>> check(File file) {
        List<Pair<String, Boolean>> checks = new ArrayList<>();

        if (file == null) {
            checks.add(Pair.of("File is null.", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("File is not null.", Boolean.TRUE));

        if (!file.exists() || file.isDirectory()) {
            checks.add(Pair.of("File does not exist.", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("File exists.", Boolean.TRUE));

        if (!hasMediaFileName(file)) {
            checks.add(Pair.of("File does not have a valid media file name.", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("File has a valid media file name.", Boolean.TRUE));

        if (!isInCorrectArchiveFolder(file)) {
            checks.add(Pair.of("File is not in correct archive folder!", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("File is in correct archive folder.", Boolean.TRUE));

        if (!hasMetadata(file)) {
            checks.add(Pair.of("File has no metadata", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("File has metadata", Boolean.TRUE));

        if (!doesMetadataDatetimeOriginalMatchFilename(file)) {
            checks.add(Pair.of("DatetimeOriginal in filename does not match the corresponding value in metadata.",
                    Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(
                Pair.of("DatetimeOriginal in filename does match the corresponding value in metadata.", Boolean.TRUE));

        if (!doesMetadataMatchExifData(file)) {
            checks.add(Pair.of("EXIF metadata does not match BiographyMetadata", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("No mismatch between EXIF metadata and BiographyMetadata", Boolean.TRUE));

        if (!isHashcodeCorrect(file)) {
            checks.add(Pair.of("SHA-1 hashcode does not match media file content", Boolean.FALSE));
            return Pair.of(Boolean.FALSE, checks);
        }
        checks.add(Pair.of("SHA-1 hashcode matches media file content", Boolean.TRUE));

        return Pair.of(Boolean.TRUE, checks);
    }

    /**
     * Has the given file a correct media file name?
     * 
     * @param file
     *            media file
     * @return Has the given file a correct media file name?
     * @throws {@link
     *             NullPointerException} if the file object is <code>null</code>
     * @throws {@link
     *             IllegalArgumentException} if the file does not exist (or is a directory)
     */
    public boolean hasMediaFileName(File file) {
        if (file == null) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException(file.getAbsolutePath());
        }
        return BiographyFileUtils.isMediaFileName(file);
    }

    /**
     * Is the given file a media file correctly placed in the archive folder?
     *
     * @param file
     *            media file
     * @return Is the given file a media file correctly placed in the archive folder?
     * @throws {@link
     *             NullPointerException} if the file object is <code>null</code>
     * @throws {@link
     *             IllegalArgumentException} if the file does not exist (or is a directory)
     * @throws {@link
     *             IllegalArgumentException} if the file does not pass {@link #hasMediaFileName(File)}!
     */
    public boolean isInCorrectArchiveFolder(File file) {
        if (!hasMediaFileName(file)) {
            throw new IllegalArgumentException(
                    String.format("File '%s' has no valid media file name.", file.getAbsolutePath()));
        }
        LocalDateTime datetimeOriginalFromArchiveFilename = BiographyFileUtils
                .getDatetimeOriginalFromArchiveFilename(file);
        File expectedFolder = new File(archiveFolder, String.format("%04d/%02d",
                datetimeOriginalFromArchiveFilename.getYear(), datetimeOriginalFromArchiveFilename.getMonthValue()));
        return expectedFolder.equals(file.getParentFile());
    }

    /**
     * Has the given media file metadata information?
     *
     * @param file
     *            media file
     * @return Has the given media file metadata information?
     * @throws {@link
     *             NullPointerException} if the file object is <code>null</code>
     * @throws {@link
     *             IllegalArgumentException} if the file does not exist (or is a directory)
     * @throws {@link
     *             IllegalArgumentException} if the file does not pass {@link #isInCorrectArchiveFolder(File)}!
     */
    public boolean hasMetadata(File file) {
        if (!isInCorrectArchiveFolder(file)) {
            throw new IllegalArgumentException(
                    String.format("File '%s' is no valid media file in archive.", file.getAbsolutePath()));
        }

        Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
        if (!mediaFileType.isPresent()) {
            LOG.warn("Unknown Media File Type."); // should never happen due to filename check
            return false;
        }

        BiographyMetadata metadata;

        if (ExifData.supports(mediaFileType.get())) {
            metadata = metadataService.readMetadataFromExif(file);
        } else {
            metadata = metadataService.readMetadataFromJsonFile(getMetadataJsonFile(file));
        }

        return metadata != null;
    }

    /**
     * Does the Datetime Original field in the metadata match the Datetime in the archive file name?
     *
     * @param file
     *            media file
     * @return Does the Datetime Original field in the metadata match the Datetime in the archive file name?
     * @throws {@link
     *             NullPointerException} if the file object is <code>null</code>
     * @throws {@link
     *             IllegalArgumentException} if the file does not exist (or is a directory)
     * @throws {@link
     *             IllegalArgumentException} if the file does not pass {@link #hasMetadata(File)}!
     */
    public boolean doesMetadataDatetimeOriginalMatchFilename(File file) {
        if (!hasMetadata(file)) {
            throw new IllegalArgumentException(
                    String.format("File '%s' does not have biography metadata attached.", file.getAbsolutePath()));
        }

        Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
        if (!mediaFileType.isPresent()) {
            LOG.warn("Unknown Media File Type."); // should never happen due to filename check
            return false;
        }

        BiographyMetadata metadata;

        if (ExifData.supports(mediaFileType.get())) {
            metadata = metadataService.readMetadataFromExif(file);
        } else {
            metadata = metadataService.readMetadataFromJsonFile(getMetadataJsonFile(file));
        }

        LocalDateTime timestampFromArchiveFileName = BiographyFileUtils.getDatetimeOriginalFromArchiveFilename(file);

        return timestampFromArchiveFileName.equals(metadata.getDateTimeOriginal().truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Does the EXIF data (if applicable) match the BiographyMetadata?
     * 
     * This method covers the DateTimeOriginal and the description fields.
     * 
     * @param file
     *            media file
     * @return Does the EXIF data (if applicable) match the BiographyMetadata?
     * @throws {@link
     *             NullPointerException} if the file object is <code>null</code>
     * @throws {@link
     *             IllegalArgumentException} if the file does not exist (or is a directory)
     * @throws {@link
     *             IllegalArgumentException} if the file does not pass
     *             {@link #doesMetadataDatetimeOriginalMatchFilename(File)}!
     */
    public boolean doesMetadataMatchExifData(File file) {
        if (!doesMetadataDatetimeOriginalMatchFilename(file)) {
            throw new IllegalArgumentException(
                    String.format("File '%s' does not have biography metadata attached.", file.getAbsolutePath()));
        }

        Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
        if (!mediaFileType.isPresent()) {
            LOG.warn("Unknown Media File Type."); // should never happen due to filename check
            return false;
        }

        if (!ExifData.supports(mediaFileType.get())) {
            return true;
        }

        BiographyMetadata metadata = metadataService.readMetadataFromExif(file);
        ExifData exifData = ExifData.of(file);

        if (!metadata.getDateTimeOriginal().equals(exifData.getDateTimeOriginal())) {
            return false;
        }

        String metadataDescription = StringUtils.trimToEmpty(metadata.getDescription());
        String exifDataDescription = StringUtils.trimToEmpty(exifData.getDescription().orElse(""));

        if (!metadataDescription.equals(exifDataDescription)) {
            return false;
        }

        return true;
    }

    /**
     * Is the hashcode in the filename matching the file content?
     * 
     * @param file
     *            media file
     * @return Is the hashcode in the filename matching the file content?
     * @throws {@link
     *             NullPointerException} if the file object is <code>null</code>
     * @throws {@link
     *             IllegalArgumentException} if the file does not exist (or is a directory)
     * @throws {@link
     *             IllegalArgumentException} if the file does not pass {@link #doesMetadataMatchExifData(File)}!
     */
    public boolean isHashcodeCorrect(File file) {
        if (!doesMetadataMatchExifData(file)) {
            throw new IllegalArgumentException(
                    String.format("File '%s' does not have biography metadata attached.", file.getAbsolutePath()));
        }

        return BiographyFileUtils.getSha1FromArchiveFilename(file).equals(BiographyFileUtils.sha1(file));
    }

    private File getMetadataJsonFile(File file) {
        return new File(file.getParent(),
                String.format("b%s.json", BiographyFileUtils.getSha1FromArchiveFilename(file)));
    }

}

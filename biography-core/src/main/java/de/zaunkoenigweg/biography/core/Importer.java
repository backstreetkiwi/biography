package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Importer {

    private static final String EXTENSION_JPG = ".jpg";
    private static final String EXTENSION_MOV = ".mov";

    private final static Log LOG = LogFactory.getLog(Importer.class);

    @Autowired
    private BiographyConfig config;

    /**
     * Imports all images from import folder into archive
     */
    public void importAll() {
        
        Map<Path, File> importMap = this.buildImportMap();
        LOG.info(String.format("Found %d images to import in %s", importMap.size(), this.config.getImportFolder().getAbsolutePath()));

        boolean anyImageExistsinArchive = importMap.keySet().stream().anyMatch(path -> {
            if (Files.exists(path)) {
                LOG.error(String.format("Path %s already exists in archive.", path));
                return true;
            }
            return false;
        });

        if (anyImageExistsinArchive) {
            throw new RuntimeException("Import cancelled. At least one image seems to exist in archive.");
        }

        importMap.forEach((path, file) -> {
            try {
                Files.createDirectories(path.getParent());
                Files.copy(Paths.get(file.toURI()), path);
                LOG.trace(String.format("Copied file %s to %s.", file.getAbsolutePath(), path));
            } catch (Exception e) {
                LOG.error(e);
                throw new RuntimeException(String.format("Error copying file %s to %s.", file, path));
            }
        });
        
        LOG.info(String.format("Copied %d images into archive.", importMap.size()));
    }

    /**
     * Builds map of images to copy.
     * 
     * @return Map (filename in archive -> file handle in import folder)
     */
    private Map<Path, File> buildImportMap() {

        Map<Path, File> importMap = new HashMap<>();

        addFilesToImportMap(EXTENSION_JPG, importMap, dateTimeOriginalFromExif, dateTimeOriginalFromFilesystem);
        addFilesToImportMap(EXTENSION_MOV, importMap, dateTimeOriginalFromFilesystem);

        return importMap;
    }

    /**
     * Adds all the image files with the given extension to a map (list of files to import)
     * @param extension file extension of image file
     * @param importMap map of files to import
     * @param timestampExtractorFuctions list of timestamp-extracting functions used to determine the timestamp of the media file
     */
    private void addFilesToImportMap(String extension, Map<Path, File> importMap, Function<File, LocalDateTime>... timestampExtractorFuctions) {
        
        File[] imageFiles = this.config.getImportFolder().listFiles(extensionFileFilter(extension));
        
        Arrays.stream(imageFiles).forEach(imageFile -> {

            Path archiveFilename = null;

            Optional<LocalDateTime> dateTimeOriginal = extractDateTimeOriginal(imageFile, timestampExtractorFuctions);
            if (!dateTimeOriginal.isPresent()) {
                LOG.error(String.format("Date/Time Original could not be extracted for %s. File will be skipped.", imageFile.getAbsolutePath()));
                return;
            }
            
            String sha1 = BiographyFileUtils.sha1(imageFile);
            
            archiveFilename = buildArchiveFilename(dateTimeOriginal.get(), sha1, extension);
            
            if (importMap.containsKey(archiveFilename)) {
                LOG.error(String.format("2 files seem to contain the same image: #1: %s, #2: %s.", imageFile.getName(),
                        importMap.get(archiveFilename).getName()));
            }
            importMap.put(archiveFilename, imageFile);
            
        });
    }

    private FileFilter extensionFileFilter(final String extension) {
        return (FileFilter) pathname -> {
            return StringUtils.endsWithIgnoreCase(pathname.getName(), extension);
        };
    }

    /**
     * Function that extracts the timestamp of an image file from the EXIF data.
     * This works only for JPEG images.
     */
    private Function<File, LocalDateTime> dateTimeOriginalFromExif = (imageFile) -> {
        ExifData exifData = ExifData.from(imageFile);
        return exifData.getDateTimeOriginal();
    };

    /**
     * Function that extracts the timestamp of an image file from the file metadata (last modified date).
     * This works for all files. 
     */
    private Function<File, LocalDateTime> dateTimeOriginalFromFilesystem = (imageFile) -> {
        return LocalDateTime.ofEpochSecond(imageFile.lastModified() / 1000, 0, ZoneOffset.UTC);
    };

    /**
     * Extracts timestamp of an image file.
     * This function applies the given extractorFunctions and returns the first valid timestamp.
     * @param imageFile
     * @param extractorFuctions
     * @return the first valid timestamp that could be found 
     */
    private Optional<LocalDateTime> extractDateTimeOriginal(final File imageFile, Function<File, LocalDateTime>... extractorFuctions) {
        return Arrays.stream(extractorFuctions)
                     .map(extractorFunction -> extractorFunction.apply(imageFile))
                     .filter(dateTimeOriginal -> dateTimeOriginal != null)
                     .findFirst();
    }

    /**
     * Builds the filename and -path of a media file in the archive
     * @param dateTimeOriginal timestamp
     * @param sha1 SHA-1 hash of the image file
     * @param extension file extension
     * @return filename and -path of a media file in the archive
     */
    private Path buildArchiveFilename(LocalDateTime dateTimeOriginal, String sha1, String extension) {
        String folderInArchive = String.format("%04d/%02d", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue());
        String fileName = String.format("%04d-%02d-%02d--%02d-%02d-%02d---%s%s", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue(),
                dateTimeOriginal.getDayOfMonth(), dateTimeOriginal.getHour(), dateTimeOriginal.getMinute(), dateTimeOriginal.getSecond(), sha1, extension);
        return Paths.get(this.config.getArchiveFolder().getPath(), folderInArchive, fileName);
    }

}

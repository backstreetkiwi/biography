package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.metadata.MetadataService;

@Component
public class ArchiveImportService {

    private final static Log LOG = LogFactory.getLog(ArchiveImportService.class);

    private MetadataService metadataService;
    private File archiveFolder;

    public ArchiveImportService(MetadataService metadataService, File archiveFolder) {
        this.metadataService = metadataService;
        this.archiveFolder = archiveFolder;
        LOG.info("ArchiveImportService started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
    }

    /**
     * Imports given media file.
     * @param album 
     */
    public ImportResult importFile(File file, boolean readLegacyDescription, String album) {

        if (!file.exists() || file.isDirectory()) {
            return ImportResult.FILE_NOT_FOUND;
        }

        Optional<MediaFileType> mediaFileType = MediaFileType.of(file);

        if (!mediaFileType.isPresent()) {
            return ImportResult.UNKNOWN_FILE_TYPE;
        }

        if (!ExifData.supports(mediaFileType.get())) {
            return ImportResult.NO_TIMESTAMP_DETECTED;
        }

        ExifData exifData = ExifData.of(file);

        if (exifData == null) {
            return ImportResult.NO_TIMESTAMP_DETECTED;
        }

        LocalDateTime dateTimeOriginal = exifData.getDateTimeOriginal();
        
        File archiveFile = BiographyFileUtils.buildArchiveFilename(archiveFolder, file,
                dateTimeOriginal, mediaFileType.get()).toFile();

        if (archiveFile.exists()) {
            return ImportResult.FILE_ALREADY_ARCHIVED;
        }

        try {
            FileUtils.copyFile(file, archiveFile);
        } catch (IOException e) {
            LOG.error("File cannot be stored in archive.", e);
        }

        setBiographyMetadata(archiveFile, dateTimeOriginal, readLegacyDescription, album);
        
        return ImportResult.SUCCESS;
    }

    private void setBiographyMetadata(File file, LocalDateTime dateTimeOriginal, boolean readLegacyDescription, String album) {
        MediaFileType mediaFileType = MediaFileType.of(file).get();
        String description = null;
        if (readLegacyDescription && ExifData.supports(mediaFileType)) {
            description = ExifData.of(file, StandardCharsets.ISO_8859_1).getDescription().orElse(null);
        }
        
        Set<Album> albums = new HashSet<>();
        if(StringUtils.isNotBlank(album)) {
            albums.add(new Album(StringUtils.trim(album)));
        }
        
        BiographyMetadata metadata = new BiographyMetadata(dateTimeOriginal, description, albums);

        if (ExifData.supports(mediaFileType)) {
            metadataService.writeMetadataIntoExif(file, metadata);
            return;
        }

        File jsonFile = new File(file.getParentFile(),
                "b" + BiographyFileUtils.getSha1FromArchiveFilename(file) + ".json");
        metadataService.writeMetadataToJsonFile(jsonFile, metadata);
    }

    public enum ImportResult {
        FILE_NOT_FOUND,
        UNKNOWN_FILE_TYPE,
        NO_TIMESTAMP_DETECTED,
        FILE_ALREADY_ARCHIVED,
        FILE_CANNOT_BE_STORED,
        SUCCESS;
    }
}

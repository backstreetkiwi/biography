package de.zaunkoenigweg.biography.core.importer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.TimestampExtractor;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.metadata.MetadataService;

public class Importer {

    private final static Log LOG = LogFactory.getLog(Importer.class);

    @Autowired
    private BiographyConfig config;

    @Autowired
    private MetadataService metadataService;

    /**
     * Imports all images from import folder into the archive.
     */
    public void importAll(boolean dryRun) {

        Set<File> mediaFilesInImportFolder = MediaFileType.all()
                .flatMap(BiographyFileUtils.streamFilesOfMediaFileType(config.getImportFolder()))
                .collect(Collectors.toSet());

        LOG.info(String.format("Found %d media files to import in %s", mediaFilesInImportFolder.size(), config.getImportFolder().getAbsolutePath()));

        Map<File, Path> filesToArchive = new HashMap<>();
        Map<Path, File> filesToArchiveReverseMap = new HashMap<>();
        ImportLog importLog = new ImportLog();

        mediaFilesInImportFolder.stream().forEach(file -> {
            MediaFileType fileType = MediaFileType.of(file).get();
            TimestampExtractor timestampExtractor = fileType.getTimestampExtractorForUntrackedFiles();
            LocalDateTime timestamp = timestampExtractor.apply(file);
            if (timestamp==null) {
                importLog.notImported(file, "No timestamp could be extracted.");
                return;
            }
            Path archiveFilename = BiographyFileUtils.buildArchiveFilename(config.getArchiveFolder(), file, timestamp, fileType);
            if (filesToArchiveReverseMap.containsKey(archiveFilename)) {
                importLog.notImported(file, String.format("File is obviously a copy of '%s'.", filesToArchiveReverseMap.get(archiveFilename).getName()));
                return;
            }
            filesToArchive.put(file, archiveFilename);
            filesToArchiveReverseMap.put(archiveFilename, file);
        });

        filesToArchive.forEach((file, archivePath) -> {
            try {
                if(Files.exists(archivePath)) {
                    importLog.notImported(file, String.format("File already exists in archive as '%s'.", archivePath));
                    return;
                }
                if(!dryRun) {
                    Files.createDirectories(archivePath.getParent());
                    Files.copy(Paths.get(file.toURI()), archivePath);
                    setBiographyMetadata(archivePath.toFile());
                    LOG.trace(String.format("Copied file %s to %s.", file.getAbsolutePath(), archivePath));
                }
                importLog.imported(file, archivePath);
            } catch (Exception e) {
                LOG.error(e);
                throw new RuntimeException(String.format("Error copying file %s to %s.", file, archivePath));
            }
        });

        LocalDateTime now = LocalDateTime.now();
        File logFile = new File(config.getImportFolder(), String.format("%04d-%02d-%02d--%02d-%02d-%02d-%09d.%slog", now.getYear(), now.getMonthValue(),
                now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond(), now.getNano(), dryRun ? "dry." : ""));
        
        String logFileContent = importLog.toString();
        
        try {
            FileUtils.writeStringToFile(logFile, logFileContent, "UTF-8");
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(String.format("Error writing logfile %s.", logFile));
        }
    }
    
    private void setBiographyMetadata(File file) {
        MediaFileType mediaFileType = MediaFileType.of(file).get();
        LocalDateTime dateTimeOriginal = mediaFileType.getTimestampExtractorForArchivedFiles().apply(file);
        String description = null;
        if(ExifData.supports(mediaFileType)) {
            description = ExifData.of(file).getDescription().orElse(null);
        }
        BiographyMetadata metadata = new BiographyMetadata(dateTimeOriginal, description, Collections.emptyList());
        metadataService.setMetadata(file, metadata);
    }
}

package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.ExifData;

/**
 * This Service manages bulk imports into the Biography archive.
 * 
 * Attention! This Service is stateful. Usually this is considered
 * an anti-pattern, but as this small WebApp is a single user application
 * this is okay ;-)
 * 
 * @author mail@nikolaus-winter.de
 */
@Component
public class ArchiveBulkImportService {

    private final static Log LOG = LogFactory.getLog(ArchiveBulkImportService.class);

    private ArchiveImportService archiveImportService;
    private File importFolder;

    private BulkImportJob importJob;

    public ArchiveBulkImportService(ArchiveImportService archiveImportService, File importFolder) {
        this.archiveImportService = archiveImportService;
        this.importFolder = importFolder;
        LOG.info("ArchiveBulkImportService started.");
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    public BulkImportJob getImportJob() {
        if(importJob==null) {
            createBulkImportJob();
        }
        return importJob;
    }
    
    public void startImport(String album) {
        importJob.setRunning(true);
        new Thread(() -> {
            importJob.getImportFiles().stream()
                .filter(importJob::isReadyToImport)
                .forEach(mediaFile -> {
                    ImportResult importResult = archiveImportService.importFile(mediaFile, true, null, album);
                    importJob.setImportResult(mediaFile, importResult);
                    importJob.setReadyToImport(mediaFile, importResult!=ImportResult.SUCCESS);
                });
            importJob.setRunning(false);
        }).start();
    }
    
    public void clearImportFolder() {
        importJob.getImportFiles().stream()
            .filter(mediaFile -> importJob.getImportResult(mediaFile)==ImportResult.SUCCESS || importJob.getImportResult(mediaFile)==ImportResult.FILE_ALREADY_ARCHIVED)
            .forEach(FileUtils::deleteQuietly);
        importJob=null;
    }
    
    private void createBulkImportJob() {
        importJob = new BulkImportJob();
        Arrays.stream(importFolder.listFiles())
                .filter(MediaFileType::isMediaFile)
                .sorted()
                .forEach(mediaFile -> {
                    importJob.add(mediaFile);
                    importJob.setMediaFileType(mediaFile, MediaFileType.of(mediaFile).orElse(null));
                    ExifData exifData = ExifData.of(mediaFile);
                    if(exifData!=null) {
                        importJob.setExifData(mediaFile, exifData);
                        LocalDateTime dateTimeOriginal = exifData.getDateTimeOriginal();
                        if(dateTimeOriginal!=null) {
                            importJob.setDateTimeOriginal(mediaFile, dateTimeOriginal);
                        }
                        importJob.setReadyToImport(mediaFile, dateTimeOriginal!=null);
                    }
                });
    }
}

package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataWrapper;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;

/**
 * This Service manages bulk imports into the Biography archive.
 * 
 * Attention! This Service is stateful. Usually this is considered
 * an anti-pattern, but as this small WebApp is meant to be a single 
 * user application this is okay ;-)
 * 
 * @author mail@nikolaus-winter.de
 */
@Component
public class ArchiveBulkImportService {

    private final static Log LOG = LogFactory.getLog(ArchiveBulkImportService.class);

    private ArchiveImportService archiveImportService;
    private ExifDataService exifDataService;
    private File importFolder;

    private BulkImportJob importJob;

    public ArchiveBulkImportService(ArchiveImportService archiveImportService, ExifDataService exifDataService, File importFolder) {
        this.archiveImportService = archiveImportService;
        this.exifDataService = exifDataService;
        this.importFolder = importFolder;
        LOG.info("ArchiveBulkImportService started.");
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    /**
     * Gets the current import job.
     * 
     * If no job exists, it is created right now. 
     * 
     * @return current import job.
     */
    public BulkImportJob getImportJob() {
        if(importJob==null) {
            createBulkImportJob();
        }
        return importJob;
    }
    
    /**
     * Starts the current import job.
     * 
     * @param album name of the album that all the imports are assigned to.
     * 
     * @return Could the job be started? If not, it is already running.
     */
    public boolean startImport(String album) {
        if(importJob.isRunning()) {
            return false;
        }
        importJob.setRunning(true);
        new Thread(() -> {
            importJob.getImportFiles().stream()
                .filter(importJob::isReadyToImport)
                .forEach(mediaFile -> {
                    ImportResult importResult = archiveImportService.importFile(mediaFile, true, importJob.getDateTimeOriginal(mediaFile), album);
                    importJob.setImportResult(mediaFile, importResult);
                    importJob.setReadyToImport(mediaFile, importResult!=ImportResult.SUCCESS);
                });
            importJob.setRunning(false);
        }).start();
        return true;
    }
    
    /**
     * Clears the import folder.
     * 
     * Clearing means that all files that are no longer used are being deleted from this object and from disk.
     * After that the import job object is deleted.
     * 
     * A file is no longer used if has already been imported, either during the current bulk import job or previously.
     */
    public void clearImportFolder() {
        importJob.getImportFiles().stream()
            .filter(mediaFile -> importJob.getImportResult(mediaFile)==ImportResult.SUCCESS || importJob.getImportResult(mediaFile)==ImportResult.FILE_ALREADY_ARCHIVED)
            .forEach(FileUtils::deleteQuietly);
        importJob=null;
    }
    
    /**
     * Creates new {@link BulkImportJob} and initializes it with the current state/content of the import folder.
     */
    private void createBulkImportJob() {
        importJob = new BulkImportJob();
        Arrays.stream(importFolder.listFiles())
                .filter(MediaFileType::isMediaFile)
                .sorted()
                .forEach(mediaFile -> {
                    importJob.add(mediaFile);
                    importJob.setMediaFileType(mediaFile, MediaFileType.of(mediaFile).orElse(null));
                    ExifDataWrapper exifData = exifDataService.getExifData(mediaFile);
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

package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifData;

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
    // TODO: use different instance of ExifDataService to keep control over cache!
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
            this.importJob = new BulkImportJob();
        }
        return importJob;
    }
    
    /**
     * Starts the current import job.
     * 
     * @return Could the job be started? If not, it is already running.
     */
    public boolean startImport() {
        if(importJob.isRunning()) {
            return false;
        }
        importJob.setRunning(true);
        new Thread(() -> {
            importJob.getImportFiles().stream()
                .filter(ImportFile::isReadyForImport)
                .forEach(importFile -> {
                    ImportResult importResult = archiveImportService.importFile(file(importFile), importFile.getDatetimeOriginal().get(), 
                                    importFile.getAlbum().orElse(null), importFile.getDescription().orElse(null));
                    importFile.setImportResult(importResult);
                });
            importJob.setRunning(false);
        }).start();
        return true;
    }
    
    /**
     * Cleans up the import folder.
     * 
     * Cleaning up (!=clearing) means that all files that are no longer used are being deleted from this object and from disk.
     * 
     * A file is no longer used if has already been imported, either during the current bulk import job or previously.
     */
    public void cleanupImportFolder() {
        Set<UUID> idsToBeDeleted = importJob.getImportFiles().stream()
            .filter(importFile -> importFile.getImportResult()==ImportResult.SUCCESS || importFile.getImportResult()==ImportResult.FILE_ALREADY_ARCHIVED)
            .map(ImportFile::getUuid)
            .collect(Collectors.toSet());
        
        importJob.remove(idsToBeDeleted);
    }
    
    
    /**
     * Clears the import folder.
     */
    public void clearImportFolder() {
        try {
            FileUtils.cleanDirectory(importFolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.importJob = new BulkImportJob();
    }
    
    
    /**
     * Creates new {@link BulkImportJob} and initializes it with the current state/content of the import folder.
     */
    // TODO null safety, error message
    public boolean upload(String originalFilename, byte[] bytes) {
        UUID uuid = UUID.nameUUIDFromBytes(bytes);
        Optional<MediaFileType> mediaFileType = MediaFileType.of(originalFilename);
        
        if(!mediaFileType.isPresent()) {
            LOG.info("Upload failed due to unknown MediaFileType: " + originalFilename);
            return false;
        }
        
        File file = file(uuid, mediaFileType.get());
        
        try {
            FileUtils.writeByteArrayToFile(file, bytes, false);
        } catch (IOException e) {
            LOG.error("File could not be stored in import folder.", e);
        }

        this.archiveImportService.generateImportThumbnails(file, uuid);

        ImportFile importFile = new ImportFile(uuid, originalFilename, mediaFileType.get());
        
        ExifData exifData = exifDataService.readExifData(file);
        if(exifData!=null) {
            importFile.setDatetimeOriginal(exifData.getDateTimeOriginal());
            importFile.setDescription(exifData.getDescription().orElse(null));
        }

        this.importJob.put(importFile);
        
        return true;
    }
    
    private File file(ImportFile importFile) {
        return file(importFile.getUuid(), importFile.getMediaFileType());
    }

    private File file(UUID uuid, MediaFileType mediaFileType) {
        return new File(this.importFolder, String.format("%s.%s", uuid, mediaFileType.getFileExtension()));
    }
    
}

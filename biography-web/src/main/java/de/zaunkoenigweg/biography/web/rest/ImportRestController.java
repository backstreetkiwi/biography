package de.zaunkoenigweg.biography.web.rest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.zaunkoenigweg.biography.core.archive.ArchiveBulkImportService;
import de.zaunkoenigweg.biography.core.archive.ArchiveImportService;
import de.zaunkoenigweg.biography.core.archive.BulkImportJob;
import de.zaunkoenigweg.biography.core.archive.ImportFile;

@RestController
public class ImportRestController {

    private final static Log LOG = LogFactory.getLog(ImportRestController.class);

    private File importFolder;
    private ArchiveBulkImportService archiveBulkImportService;
    private ArchiveImportService archiveImportService;

    public ImportRestController(ArchiveBulkImportService archiveBulkImportService, ArchiveImportService archiveImportService, File importFolder) {
        this.archiveBulkImportService = archiveBulkImportService;
        this.archiveImportService = archiveImportService;
        this.importFolder = importFolder;
        LOG.info("ImportRestController started.");
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    /**
     * Process upload of files.
     * 
     * @param files uploaded files
     * @return
     */
    @CrossOrigin
    @PostMapping("/rest/import/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files) {

        Arrays.stream(files).forEach(uploadFile -> {
            
            try {
                this.archiveBulkImportService.upload(uploadFile.getOriginalFilename(), uploadFile.getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        return "OK";
    }

    @CrossOrigin
    @PostMapping("/rest/import/clear/")
    public String clearImportJob() {
        this.archiveBulkImportService.clearImportFolder();
        return "OK";
    }
    
    @CrossOrigin
    @PostMapping("/rest/import/cleanup/")
    public String cleanUpImportJob() {
        this.archiveBulkImportService.cleanupImportFolder();
        return "OK";
    }
    
    @CrossOrigin
    @PostMapping("/rest/import/start/")
    public String startImportJob() {
        this.archiveBulkImportService.startImport();
        return "OK";
    }
    
    @CrossOrigin
    @RequestMapping("/rest/import/job/state")
    public Map<String,String> getImportJobState() {
        BulkImportJob importJob = this.archiveBulkImportService.getImportJob();
        Map<String,String> result = new HashMap<>();
        result.put("state", importJob.isRunning() ? "running" : "stopped");
        return result;
    }
    
    @CrossOrigin
    @RequestMapping("/rest/import/files/")
    public List<Map<String,String>> get() {
        BulkImportJob importJob = this.archiveBulkImportService.getImportJob();
        return importJob.getImportFiles().stream().map(file -> {
            Map<String,String> map = new HashMap<>();
            map.put("id", file.getUuid().toString());
            map.put("name", file.getOriginalFileName());
            map.put("datetimeOriginal", file.getDatetimeOriginal().map(LocalDateTime::toString).orElse(""));
            map.put("description", file.getDescription().orElse(""));
            map.put("importResult", file.getImportResult()!=null ? file.getImportResult().toString() : "");
            map.put("mediaFileType", file.getMediaFileType()!=null ? file.getMediaFileType().toString() : "");
            map.put("album", file.getAlbum().orElse(""));
            return map;
        })
        .collect(Collectors.toList());
    }
    
    // TODO handle return stuff well & errors
    @CrossOrigin
    @PutMapping("/rest/import/files/{id}")
    public String save(
                    @PathVariable("id") UUID id, 
                    @RequestParam(name="description", required=false, defaultValue="") String description,
                    @RequestParam(name="album", required=false, defaultValue="") String album,
                    @RequestParam(name="datetimeOriginal", required=false, defaultValue="") String datetimeOriginalParam) {
        BulkImportJob importJob = this.archiveBulkImportService.getImportJob();
        Optional<ImportFile> importFile = importJob.get(id);
        importFile.get().setDescription(description);
        importFile.get().setAlbum(album);
        try {
            LocalDateTime datetimeOriginal = LocalDateTime.parse(datetimeOriginalParam);
            if(datetimeOriginal!=null) {
                importFile.get().setDatetimeOriginal(datetimeOriginal);
            }
        } catch (Exception e) {
            // DatetimeOriginal unchanged...
        }
        return "OK";
    }
    
    
    
}

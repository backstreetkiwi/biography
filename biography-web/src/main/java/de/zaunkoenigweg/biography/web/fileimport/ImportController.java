package de.zaunkoenigweg.biography.web.fileimport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.zaunkoenigweg.biography.core.archive.ArchiveBulkImportService;

@Controller
public class ImportController {

    private final static Log LOG = LogFactory.getLog(ImportController.class);

    private File importFolder;
    private ArchiveBulkImportService archiveBulkImportService;

    public ImportController(ArchiveBulkImportService archiveBulkImportService, File importFolder) {
        this.archiveBulkImportService = archiveBulkImportService;
        this.importFolder = importFolder;
        LOG.info("ImportController started.");
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    /**
     * Shows import page.
     * 
     * @param model
     * @return
     */
    @GetMapping("/import")
    public String importMain(Model model) {
        model.addAttribute("selectedMenuItem", "IMPORT");
        model.addAttribute("importJob", archiveBulkImportService.getImportJob());
        return "import/index";
    }

    /**
     * Process upload of files.
     * 
     * @param files uploaded files
     * @return
     */
    @PostMapping("/import/add")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files) {

        Arrays.stream(files).forEach(uploadFile -> {
            
            File fileInImportFolder = fileInImportFolder(uploadFile);

            try {
                FileUtils.writeByteArrayToFile(fileInImportFolder, uploadFile.getBytes(), false);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        this.archiveBulkImportService.clearImportFolder();
        return "redirect:/import";
    }
    
    /**
     * Sets dateTimeOriginal on file.
     * 
     * @param filename Name of file in import folder
     * @param dateTimeOriginalParam new timestamp
     * @return
     */
    @PostMapping("/import/file/{fileName}/setdatetimeoriginal")

    public String setDateTimeOriginal(@PathVariable("fileName")String filename, @RequestParam(name="dateTimeOriginal", required=true) String dateTimeOriginalParam) {

        LocalDateTime dateTimeOriginal = null;
        try {
            dateTimeOriginal = LocalDateTime.parse(dateTimeOriginalParam);
        } catch (Exception e1) {
            return "redirect:/import";
        }

        File fileInImportFolder = new File(importFolder, filename);
        this.archiveBulkImportService.getImportJob().setDateTimeOriginal(fileInImportFolder, dateTimeOriginal);
        this.archiveBulkImportService.getImportJob().setReadyToImport(fileInImportFolder, true);
        return "redirect:/import";
    }
    
    /**
     * Starts import.
     * 
     * @param albumName Name of album to write to metadata during import.
     * @return
     */
    @PostMapping("/import/start")
    public String startImport(@RequestParam("album") String albumName) {
        this.archiveBulkImportService.startImport(albumName);
        return "redirect:/import";
    }

    /**
     * Clears import folder.
     * 
     * All files that are already imported or that are duplicates of already imported files are deleted from the import folder.
     * @return
     */
    @PostMapping("/import/clear")
    public String clearImport() {
        this.archiveBulkImportService.clearImportFolder();
        return "redirect:/import";
    }

    /**
     * Creates a non-existing file handle inside the {@link #importFolder} for the given uploaded file.
     * 
     * @param uploadFile uploaded file
     * @return free file handle.
     */
    private File fileInImportFolder(MultipartFile uploadFile) {
        File fileInImportFolder = new File(importFolder, uploadFile.getOriginalFilename());   
        if(!fileInImportFolder.exists()) {
            return fileInImportFolder;
        }
        int indexOfFileExtension = StringUtils.lastIndexOf(uploadFile.getOriginalFilename(), ".");
        String fileNameWithIndexPattern = uploadFile.getOriginalFilename() + "_" + "%05d";
        if(indexOfFileExtension>=0) {
            fileNameWithIndexPattern = StringUtils.substring(uploadFile.getOriginalFilename(), 0, indexOfFileExtension) + "_" + "%05d" + StringUtils.substring(uploadFile.getOriginalFilename(), indexOfFileExtension);
        }
        int suffix = 1;
        while(fileInImportFolder.exists()) {
            fileInImportFolder = new File(importFolder, String.format(fileNameWithIndexPattern, suffix++));
        }
        return fileInImportFolder;
    }
}

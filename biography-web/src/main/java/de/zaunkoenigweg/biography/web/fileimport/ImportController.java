package de.zaunkoenigweg.biography.web.fileimport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.zaunkoenigweg.biography.core.archiveimport.BulkImportService;

@Controller
public class ImportController {

    private final static Log LOG = LogFactory.getLog(ImportController.class);

    private File importFolder;
    private BulkImportService archiveBulkImportService;

    public ImportController(BulkImportService archiveBulkImportService, File importFolder) {
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
     * Saves import form.
     * 
     * @return
     */
//    @PostMapping("/import/save")
//    public String saveImport(HttpServletRequest request) {
//        this.archiveBulkImportService.getImportJob().getImportFiles().stream().forEach(file -> {
//            String importFileName = file.getName();
//            String album = request.getParameter(importFileName + "-album");
//            if(album!=null) {
//                this.archiveBulkImportService.getImportJob().setAlbum(file, album);
//            }
//            String description = request.getParameter(importFileName + "-description");
//            if(description!=null) {
//                this.archiveBulkImportService.getImportJob().setDescription(file, description);
//            }
//            String overwriteDateTimeOriginal = request.getParameter(importFileName + "-overwrite-datetime-original");
//            if(overwriteDateTimeOriginal!=null) {
//                LocalDateTime dateTimeOriginal = null;
//                try {
//                    dateTimeOriginal = LocalDateTime.parse(request.getParameter(importFileName + "-datetime-original")).truncatedTo(ChronoUnit.MILLIS);
//                    this.archiveBulkImportService.getImportJob().setDateTimeOriginal(file, dateTimeOriginal);
//                } catch (DateTimeParseException e) {
//                    // that's okay...
//                }
//            }
//        });
//        
//        return "redirect:/import";
//    }
    
    
    /**
     * Starts import.
     * 
     * @return
     */
    @PostMapping("/import/start")
    public String startImport(HttpServletRequest request) {
        this.archiveBulkImportService.startImport();
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

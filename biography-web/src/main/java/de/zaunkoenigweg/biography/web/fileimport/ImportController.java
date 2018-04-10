package de.zaunkoenigweg.biography.web.fileimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.zaunkoenigweg.biography.core.archive.ArchiveBulkImportService;
import de.zaunkoenigweg.biography.core.archive.BulkImportJob;

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

    @GetMapping("/import")
    public String importMain(Model model) {
        BulkImportJob importJob = archiveBulkImportService.getImportJob();
        model.addAttribute("importJob", importJob);
        return "import/index";
    }

    @PostMapping("/import/add")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files, RedirectAttributes redirectAttributes) {

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

    @PostMapping("/import/start")
    public String startImport(@RequestParam("album") String album, Model model) {
        this.archiveBulkImportService.startImport(album);
        return "redirect:/import";
    }

    @PostMapping("/import/clear")
    public String clearImport(Model model) {
        this.archiveBulkImportService.clearImportFolder();
        return "redirect:/import";
    }
}

package de.zaunkoenigweg.biography.web.fileimport;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        File localFile = new File(importFolder, file.getOriginalFilename());

        try {
            FileUtils.writeByteArrayToFile(localFile, file.getBytes(), false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.archiveBulkImportService.clearImportFolder();
        return "redirect:/import";
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

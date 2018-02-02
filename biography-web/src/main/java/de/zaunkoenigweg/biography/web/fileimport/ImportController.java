package de.zaunkoenigweg.biography.web.fileimport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

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

import de.zaunkoenigweg.biography.core.archive.ArchiveImportService;
import de.zaunkoenigweg.biography.core.archive.ArchiveImportService.ImportResult;

@Controller
public class ImportController {

    private final static Log LOG = LogFactory.getLog(ImportController.class);

    private File importFolder;

    private ArchiveImportService archiveImportService;

    public ImportController(ArchiveImportService archiveImportService, File importFolder) {
        this.archiveImportService = archiveImportService;
        this.importFolder = importFolder;
        LOG.info("ImportController started.");
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    @GetMapping("/import")
    public String importMain(Model model) {
        return "import/index";
    }

    @PostMapping("/import")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            @RequestParam(name="readLegacyDescription", required=false, defaultValue="false") boolean readLegacyDescription, @RequestParam(name="dateTimeOriginal", required=false) String dateTimeOriginalParam, @RequestParam("album") String album, RedirectAttributes redirectAttributes) {

        LocalDateTime dateTimeOriginal = null;
        try {
            dateTimeOriginal = LocalDateTime.parse(dateTimeOriginalParam);
        } catch (Exception e1) {
        }
        
        File localFile = new File(importFolder, file.getOriginalFilename());

        ImportResult result;
        try {
            FileUtils.writeByteArrayToFile(localFile, file.getBytes(), false);
            result = archiveImportService.importFile(localFile, readLegacyDescription, dateTimeOriginal, album);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "import/index";
        }

        redirectAttributes.addFlashAttribute("importResult", result);
        
        return "redirect:/import-success";
    }

    @GetMapping("/import-success")
    public String importSuccess(Model model) {
        return "import/success";
    }

}

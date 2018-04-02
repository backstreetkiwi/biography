package de.zaunkoenigweg.biography.web.fileimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        File localFile = new File(importFolder, file.getOriginalFilename());

        if (localFile.exists()) {
            // TODO Proper error message
            return "redirect:/alreadyexists";
        }

        try {
            FileUtils.writeByteArrayToFile(localFile, file.getBytes(), false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (StringUtils.equals(file.getContentType(), "application/zip")) {
            unzip(localFile);
            FileUtils.deleteQuietly(localFile);
            this.archiveBulkImportService.clearImportFolder();
            return "redirect:/import";
        }

        this.archiveBulkImportService.clearImportFolder();
        return "redirect:/import";
    }

    // TODO: Do it better ;-)
    // TODO: Handle existing files...
    private void unzip(File zipFile) {
        
        byte[] buffer = new byte[1024];
        
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                if (ze.isDirectory() || ze.getName().startsWith(".") || ze.getName().startsWith("_")
                        || ze.getName().contains("/")) {
                    ze = zis.getNextEntry();
                    continue;
                }
                File newFile = new File(importFolder + File.separator + ze.getName());

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                ze = zis.getNextEntry();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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

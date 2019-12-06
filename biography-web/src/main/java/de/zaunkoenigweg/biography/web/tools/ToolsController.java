package de.zaunkoenigweg.biography.web.tools;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.archiveimport.ImportService;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.index.IndexingService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;
import de.zaunkoenigweg.biography.web.console.Console;
import de.zaunkoenigweg.biography.web.console.Consoles;

@Controller
public class ToolsController {

    private final static Log LOG = LogFactory.getLog(ToolsController.class);

    private File archiveFolder;

    private IndexingService indexingService;

    private ImportService archiveImportService;

    private ArchiveValidationService archiveValidationService;

    private ArchiveMetadataService archiveMetadataService;

    private ExifDataService exifDataService;

    private Consoles consoles;

    public ToolsController(File archiveFolder, ArchiveValidationService archiveValidationService,
                    ArchiveMetadataService archiveMetadataService,
                    IndexingService indexingService, ImportService archiveImportService, ExifDataService exifDataService, Consoles consoles) {
        this.archiveFolder = archiveFolder;
        this.archiveValidationService = archiveValidationService;
        this.archiveMetadataService = archiveMetadataService;
        this.indexingService = indexingService;
        this.archiveImportService = archiveImportService;
        this.exifDataService = exifDataService;
        this.consoles = consoles;
        LOG.info("ToolsController started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
    }

    @RequestMapping("/tools")
    public String statistics(Model model) {
        model.addAttribute("selectedMenuItem", "TOOLS");
        return "tools/index";
    }

    @RequestMapping("/tools/fill-exif-cache")
    public String fillExifCache(Model model) {

        Console console = consoles.create("fill EXIF cache");

        new Thread(() -> {
            this.exifDataService.fillCacheFromArchive(console::println);
            console.close();
        }).start();

        return "redirect:/console";

    }

    @RequestMapping("/tools/clear-exif-cache")
    public String clearExifCache(Model model) {

        Console console = consoles.create("clear EXIF cache");

        new Thread(() -> {
            //this.exifDataService.clearCache();
            console.println("Clearing of the cache not supported...");
            console.close();
        }).start();

        return "redirect:/console";

    }

    @RequestMapping("/tools/inspect-archive")
    public String statisticsInspectArchive(Model model) {

        Console console = consoles.create("inspect archive");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            int totalNumberOfFiles = mediaFiles.size();
            AtomicInteger numberOfCorruptFiles = new AtomicInteger(0);

            mediaFiles.stream().forEach(file -> {

                Pair<Boolean, List<Pair<String, Boolean>>> check = archiveValidationService.check(file);
                if (check.getLeft()) {
                    console.println(String.format("File '%s' -> [OK]", file.getAbsolutePath()));
                } else {
                    numberOfCorruptFiles.incrementAndGet();
                    ;
                    console.println(String.format("ERROR in file '%s'", file.getAbsolutePath()));
                    check.getRight().stream().forEach(pair -> {
                        console.println(String.format("%-100s [%s]", pair.getLeft(), pair.getRight() ? "OK" : "ERROR"));
                    });
                    console.println("");
                }

            });

            console.println(String.format("%n%nValidated files #: %d, # of corrupt files: %d%n", totalNumberOfFiles,
                            numberOfCorruptFiles.get()));
            console.close();
        }).start();

        return "redirect:/console";

    }

    @RequestMapping("/tools/fix-hashcodes")
    public String fixHashcode(Model model) {

        Console console = consoles.create("fix hashcodes");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            int totalNumberOfFiles = mediaFiles.size();
            AtomicInteger numberOfCorruptFiles = new AtomicInteger(0);

            mediaFiles.stream().forEach(file -> {

                archiveMetadataService.fixSha1InMetadata(file);
                console.println(String.format("File '%s' -> [OK]", file.getAbsolutePath()));
            });

            console.println(String.format("%n%nValidated files #: %d, # of corrupt files: %d%n", totalNumberOfFiles,
                            numberOfCorruptFiles.get()));
            console.close();
        }).start();

        return "redirect:/console";

    }

    @RequestMapping("/tools/generate-missing-thumbnails")
    public String generateMissingThumbnails(Model model) {

        Console console = consoles.create("generate thumbnails");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            // TODO Thumbnails for every media file type
            mediaFiles.stream().filter(MediaFileType.JPEG::isTypeOf).forEach(file -> {
                console.println(
                                String.format("File '%s' -> [%s]", file.getName(), archiveImportService.generateThumbnails(file)));
            });

            console.close();
        }).start();

        return "redirect:/console";

    }

    @GetMapping("/tools/bulk-tagging")
    public String bulkTagging(Model model) {

        model.addAttribute("selectedMenuItem", "TOOLS");
        return "tools/bulk-tagging";
    }

    @PostMapping("/tools/bulk-tagging")
    public String bulkTagging(Model model, @RequestParam("album") String albumname, @RequestParam("files") String files) {

        Console console = consoles.create("bulk-tagging " + albumname);

        new Thread(() -> {
            console.println(String.format("Bulk tagging '%s'", albumname));
            if (StringUtils.isBlank(albumname)) {
                console.println("Album must not be blank.");
                return;
            }
            Album album = new Album(albumname);
            Arrays.stream(StringUtils.split(files)).forEach(filepath -> {
                String filename = StringUtils.trim(StringUtils.substringAfterLast(filepath, "/"));
                File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
                if (archiveFile == null) {
                    console.println(String.format("'%s' is not a valid archive file.", filename));
                }
                console.println(String.format("Tagging '%s'", archiveFile));

                archiveMetadataService.addAlbum(archiveFile, album);

                // indexingService.reIndex(archiveFile);

            });
            console.close();
        }).start();

        return "redirect:/console";

    }

    @RequestMapping("/tools/inspect-file/{file}")
    public String dumpFileDetails(Model model, @PathVariable("file") String filename) {

        Console console = consoles.create("inspect-file " + filename);

        new Thread(() -> {
            File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);

            Pair<Boolean, List<Pair<String, Boolean>>> check = archiveValidationService.check(archiveFile);
            if (!check.getLeft()) {
                console.println(String.format("ERROR in file '%s'", archiveFile.getAbsolutePath()));
                check.getRight().stream().forEach(pair -> {
                    console.println(String.format("%-100s [%s]", pair.getLeft(), pair.getRight() ? "OK" : "ERROR"));
                });
                console.println("");
                console.close();
                return;
            }

            console.println(String.format("File '%s' -> [OK]%n", archiveFile.getAbsolutePath()));

            BiographyMetadata metadata = archiveMetadataService.getMetadata(archiveFile);

            console.println(String.format("Filename: %s", archiveFile));
            console.println(String.format("DateTimeOriginal: %s", metadata.getDateTimeOriginal()));
            console.println(String.format("Description: %s", metadata.getDescription()));
            console.println(String.format("Albums: %s", metadata.getAlbums()));

            console.close();

        }).start();

        return "redirect:/console";
    }

    @RequestMapping("/tools/rebuild-index")
    public String rebuildIndex(Model model) {

        Console console = consoles.create("Rebuild Solr index");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            console.println(mediaFiles.size() + " to index.");
            // TODO incremental output to console?
            this.indexingService.rebuildIndex();
            console.println("Finished.");
            console.close();
        }).start();

        return "redirect:/console";
    }

}
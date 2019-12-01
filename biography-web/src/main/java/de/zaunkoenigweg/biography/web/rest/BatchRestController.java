package de.zaunkoenigweg.biography.web.rest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.archiveimport.ImportService;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.index.IndexingService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;
import de.zaunkoenigweg.biography.web.console.Console;
import de.zaunkoenigweg.biography.web.console.Consoles;

@RestController
public class BatchRestController {

    private final static Log LOG = LogFactory.getLog(BatchRestController.class);

    private Consoles consoles;
    private File archiveFolder;
    private IndexingService indexingService;
    private ImportService archiveImportService;
    private ExifDataService exifDataService;
    private ArchiveValidationService archiveValidationService;

    public BatchRestController(Consoles consoles, IndexingService indexingService, ImportService archiveImportService, ExifDataService exifDataService, ArchiveValidationService archiveValidationService, File archiveFolder) {
        this.consoles = consoles;
        this.indexingService = indexingService;
        this.archiveImportService = archiveImportService;
        this.archiveFolder = archiveFolder;
        this.exifDataService = exifDataService;
        this.archiveValidationService = archiveValidationService;
        LOG.info("BatchRestController started.");
    }

    @CrossOrigin
    @RequestMapping("/rest/batch/")
    public List<Map<String, String>> get() {
        return consoles.getAll()
                        .stream()
                        .map(this::toRest)
                        .collect(Collectors.toList());
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/{consoleid}")
    public Map<String, String> get(@PathVariable("consoleid")Integer consoleId) {
        Console console = consoles.get(consoleId);
        return toRest(console);
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/{consoleid}/console")
    public String getConsole(@PathVariable("consoleid")Integer consoleId) {
        Console console = consoles.get(consoleId);
        return console.getContent();
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/start/fill-exif-cache")
    public Map<String, String> startFillExifCache() {

        Console console = consoles.create("Fill EXIF cache");

        new Thread(() -> {
            this.exifDataService.fillCacheFromArchive(console::println);
            console.println("Finished.");
            console.close();
        }).start();
        
        return toRest(console); 
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/start/inspect-archive")
    public Map<String, String> startInspectArchive() {

        Console console = consoles.create("Inspect Archive");

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
        
        return toRest(console); 
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/start/rebuild-index")
    public Map<String, String> startRebuildIndex() {

        Console console = consoles.create("Rebuild Solr index");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            console.println(mediaFiles.size() + " to index.");
            // TODO incremental output to console?
            this.indexingService.rebuildIndex();
            console.println("Finished.");
            console.close();
        }).start();
        
        return toRest(console); 
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/start/generate-all-thumbnails")
    public Map<String, String> generateAllThumbnails() {

        Console console = consoles.create("generate thumbnails");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            // TODO Thumbnails for every media file type
            mediaFiles.stream().filter(MediaFileType.JPEG::isTypeOf).forEach(file -> {
                console.println(
                                String.format("File '%s' -> [%s]", file.getName(), archiveImportService.generateThumbnails(file, true)));
            });

            console.close();
        }).start();
        
        return toRest(console); 
    }
    
    @CrossOrigin
    @RequestMapping("/rest/batch/start/generate-missing-thumbnails")
    public Map<String, String> generateMissingThumbnails() {

        Console console = consoles.create("generate thumbnails");

        new Thread(() -> {
            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

            // TODO Thumbnails for every media file type
            mediaFiles.stream().filter(MediaFileType.JPEG::isTypeOf).forEach(file -> {
                console.println(
                                String.format("File '%s' -> [%s]", file.getName(), archiveImportService.generateThumbnails(file, false)));
            });

            console.close();
        }).start();
        
        return toRest(console); 
    }
    
    private Map<String, String> toRest(Console console) {
        Map<String, String> result = new HashMap<>();
        result.put("title", console.getTitle());
        result.put("startTime", console.getStartTime().toString());
        result.put("closed", Boolean.toString(console.isClosed()));
        return result;
    }
    
}

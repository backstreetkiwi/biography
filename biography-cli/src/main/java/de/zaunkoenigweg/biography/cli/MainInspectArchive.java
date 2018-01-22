package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

public class MainInspectArchive {
    
    private final static Log LOG = LogFactory.getLog(MainInspectArchive.class);

    private static ArchiveValidationService archiveService;
    private static BiographyConfig biographyConfig;

    public static void main(String[] args) {
//        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
//
//        archiveService = springContext.getBean(ArchiveValidationService.class);
//        biographyConfig = springContext.getBean(BiographyConfig.class);
//        
//        System.out.printf("%n%nValidating all media files of archive %s%n%n", biographyConfig.getArchiveFolder().toString());
//        
//        inspectArchive();
//
//        springContext.close();
//        System.out.println("Bye...");
    }

    private static void inspectArchive() {
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));

        // TODO utils should give me a stream!
        List<File> mediaFiles = BiographyFileUtils.getMediaFiles(biographyConfig.getArchiveFolder());
        
        int totalNumberOfFiles = mediaFiles.size();
        MutableInt numberOfCorruptFiles = new MutableInt(0);
        
        mediaFiles.stream().forEach(file -> {
        	
            Pair<Boolean, List<Pair<String, Boolean>>> check = archiveService.check(file);
            if(check.getLeft()) {
            	System.out.printf("File '%s' -> [OK]%n", file.getAbsolutePath());
            } else {
            	numberOfCorruptFiles.increment();;
            	System.out.printf("ERROR in file '%s'%n", file.getAbsolutePath());
                check.getRight().stream().forEach(pair -> {
                	printMessage(pair.getLeft(), pair.getRight());
                });
            }
            
        });
        
        System.out.printf("%n%nValidated files #: %d, # of corrupt files: %d%n%n", totalNumberOfFiles, numberOfCorruptFiles.getValue());
        
        
//        try {
//            System.out.println("Please enter full media file path(s): ");
//            List<File> mediaFilePaths = new ArrayList<>();
//            String mediaFilePath = stdInReader.readLine();
//            while(StringUtils.isNotBlank(mediaFilePath)) {
//                mediaFilePaths.add(new File(mediaFilePath));
//                mediaFilePath = stdInReader.readLine();
//            }
//            System.out.printf("%d media files entered...%n", mediaFilePaths.size());
//            mediaFilePaths.stream().forEach(MainInspectArchive::inspectFile);
//            System.out.println();
//            System.out.println();
//        } catch (IOException e) {
//            LOG.error("Read from System.in failed.", e);
//            return;
//        }
    }
    
    private static void inspectFile(File file) {
        
        System.out.printf("%n%nInspecting file %s ...%n", file.toString());
        
        Pair<Boolean, List<Pair<String, Boolean>>> check = archiveService.check(file);
        
        check.getRight().stream().forEach(pair -> {
        	printMessage(pair.getLeft(), pair.getRight());
        });
        printMessage("SUMMARY --------------------------------------> ", check.getLeft());
    }

    private static void printMessage(String message, boolean okay) {
        System.out.printf("%-100s [%s]%n", message, okay ? "OK" : "ERROR");
    }
}

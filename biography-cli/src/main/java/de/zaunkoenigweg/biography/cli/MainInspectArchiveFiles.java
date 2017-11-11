package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;

public class MainInspectArchiveFiles {
    
    private final static Log LOG = LogFactory.getLog(MainInspectArchiveFiles.class);

    private static ArchiveValidationService archiveService;
    private static BiographyConfig biographyConfig;

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);

        archiveService = springContext.getBean(ArchiveValidationService.class);
        biographyConfig = springContext.getBean(BiographyConfig.class);
        
        System.out.printf("%n%nValidating the given files in context of archive %s%n%n", biographyConfig.getArchiveFolder().toString());
        
        inspectArchiveFiles();

        springContext.close();
        System.out.println("Bye...");
    }

    private static void inspectArchiveFiles() {
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            System.out.println("Please enter full media file path(s): ");
            List<File> mediaFilePaths = new ArrayList<>();
            String mediaFilePath = stdInReader.readLine();
            while(StringUtils.isNotBlank(mediaFilePath)) {
                mediaFilePaths.add(new File(mediaFilePath));
                mediaFilePath = stdInReader.readLine();
            }
            System.out.printf("%d media files entered...%n", mediaFilePaths.size());
            mediaFilePaths.stream().forEach(MainInspectArchiveFiles::inspectFile);
            System.out.println();
            System.out.println();
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
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

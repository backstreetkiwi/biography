package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;

public class MainDumpBiographyMetadata {
    
    private final static Log LOG = LogFactory.getLog(MainDumpBiographyMetadata.class);

    private static ArchiveMetadataService archiveMetadataService;
    private static BiographyConfig biographyConfig;

    public static void main(String[] args) {
//        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
//
//        archiveMetadataService = springContext.getBean(ArchiveMetadataService.class);
//        biographyConfig = springContext.getBean(BiographyConfig.class);
//        
//        System.out.printf("%n%nDumping the given files in context of archive %s%n%n", biographyConfig.getArchiveFolder().toString());
//        
//        dump();
//
//        springContext.close();
//        System.out.println("Bye...");
    }

    private static void dump() {
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
            mediaFilePaths.stream().forEach(MainDumpBiographyMetadata::dumpBiographyMetadata);
            System.out.println();
            System.out.println();
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
    }
    
    private static void dumpBiographyMetadata(File file) {
        
        System.out.printf("%n%nDumping BiographyMetadata for file %s ...%n", file.toString());

        BiographyMetadata metadata = null;
        try {
			metadata = archiveMetadataService.getMetadata(file);
		} catch (Exception e) {
			System.out.printf("%nError reading metadata :-( %n%n");
			return;
		}
        
        System.out.printf("%nDescription: %s", metadata.getDescription());
        System.out.printf("%nDateTimeOriginal: %s", metadata.getDateTimeOriginal());
        System.out.printf("%nAlbums: %s", metadata.getAlbums());
    }

}

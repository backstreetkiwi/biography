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

import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.metadata.MetadataService;

public class MainSetDescriptionOnFiles {
    
    private final static Log LOG = LogFactory.getLog(MainSetDescriptionOnFiles.class);
    
    private static MetadataService metadataService;

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        System.out.println("WARNING! This program overwrites existing descriptions.");

        metadataService = springContext.getBean(MetadataService.class);
        
        setDescriptionOnFiles();

        springContext.close();
        System.out.println("Bye...");
    }

    public static void setDescriptionOnFiles() {
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        
        String input = null;
        
        System.out.print("Please enter Description: ");
        try {
            input = stdInReader.readLine();
            if(StringUtils.isBlank(input)) {
                System.out.println("Description must not be empty.");
                return;
            }
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
        
        final String description = input;
        
        try {
            System.out.println("Please enter full media file path(s): ");
            List<File> mediaFilePaths = new ArrayList<>();
            String mediaFilePath = stdInReader.readLine();
            while(StringUtils.isNotBlank(mediaFilePath)) {
                mediaFilePaths.add(new File(mediaFilePath));
                mediaFilePath = stdInReader.readLine();
            }
            System.out.printf("%d media files entered...%n", mediaFilePaths.size());
            mediaFilePaths.forEach(file -> {
                ExifData.setDescription(file, description);
                BiographyMetadata oldMetadata = metadataService.getMetadata(file);
                BiographyMetadata newMetadata = new BiographyMetadata(oldMetadata.getDateTimeOriginal(), description, oldMetadata.getAlbums());
                metadataService.setMetadata(file, newMetadata);
            });
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
    }

}

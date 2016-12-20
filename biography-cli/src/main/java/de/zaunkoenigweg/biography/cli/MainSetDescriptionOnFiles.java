package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.metadata.ExifData;

public class MainSetDescriptionOnFiles {
    
    private final static Log LOG = LogFactory.getLog(MainSetDescriptionOnFiles.class);

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);

        setAlbumOnFiles();

        springContext.close();
        System.out.println("Bye...");
    }

    public static void setAlbumOnFiles() {
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
            boolean hasAnyFileAlreadyADescription = mediaFilePaths.stream().map(ExifData::of).map(ExifData::getDescription).anyMatch(Optional::isPresent);
            if(hasAnyFileAlreadyADescription) {
                System.out.println("Some files already have a description field set.");
                return;
            }
            mediaFilePaths.forEach(file -> {
                ExifData.setDescription(file, description);
            });
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
    }

}

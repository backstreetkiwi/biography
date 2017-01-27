package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.metadata.MetadataService;

public class MainCreateMetadataOnFiles {
    
    private final static Log LOG = LogFactory.getLog(MainCreateMetadataOnFiles.class);

    private static MetadataService metadataService;

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);

        metadataService = springContext.getBean(MetadataService.class);
        
        setAlbumOnFiles();

        springContext.close();
        System.out.println("Bye...");
    }

    public static void setAlbumOnFiles() {
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        
        String albumId = null;
        
        System.out.println("Existing Biography Metadata will be destroyed on touched files!!!");
        try {
            System.out.println("Please enter full media file path(s): ");
            List<File> mediaFilePaths = new ArrayList<>();
            String mediaFilePath = stdInReader.readLine();
            while(StringUtils.isNotBlank(mediaFilePath)) {
                mediaFilePaths.add(new File(mediaFilePath));
                mediaFilePath = stdInReader.readLine();
            }
            System.out.printf("%d media files entered...%n", mediaFilePaths.size());
            mediaFilePaths.stream().forEach(file -> {
                MediaFileType mediaFileType = MediaFileType.of(file).get();
                LocalDateTime dateTimeOriginal = mediaFileType.getTimestampExtractorForArchivedFiles().apply(file);
                String description = null;
                if(ExifData.supports(mediaFileType)) {
                    description = ExifData.of(file).getDescription().orElse(null);
                }
                BiographyMetadata metadata = new BiographyMetadata(dateTimeOriginal, description, Collections.emptyList());
                metadataService.setMetadata(file, metadata);
            });
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
    }

}

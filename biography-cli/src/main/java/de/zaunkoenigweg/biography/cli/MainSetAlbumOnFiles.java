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

import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveMetadataService;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.MetadataService;

public class MainSetAlbumOnFiles {
    
    private final static Log LOG = LogFactory.getLog(MainSetAlbumOnFiles.class);

    private static ArchiveMetadataService archiveMetadataService;

    public static void main(String[] args) {
//        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
//
//        archiveMetadataService = springContext.getBean(ArchiveMetadataService.class);
//        
//        setAlbumOnFiles();
//
//        springContext.close();
//        System.out.println("Bye...");
    }

//    public static void setAlbumOnFiles() {
//        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
//        
//        String albumId = null;
//        
//        System.out.println("Existing Biography Metadata will be destroyed on touched files!!!");
//        System.out.print("Please enter Album ID: ");
//        try {
//            albumId = stdInReader.readLine();
//            if(StringUtils.isBlank(albumId)) {
//                System.out.println("Album ID must not be empty.");
//                return;
//            }
//        } catch (IOException e) {
//            LOG.error("Read from System.in failed.", e);
//            return;
//        }
//        
//        Album album;
//        try {
//            album = Album.fromId(albumId);
//            System.out.println(album.toJson());
//            System.out.println();
//        } catch (IllegalArgumentException e) {
//            System.out.println("Album ID is not valid.");
//            return;
//        }
//
//        try {
//            System.out.println("Please enter full media file path(s): ");
//            List<File> mediaFilePaths = new ArrayList<>();
//            String mediaFilePath = stdInReader.readLine();
//            while(StringUtils.isNotBlank(mediaFilePath)) {
//                mediaFilePaths.add(new File(mediaFilePath));
//                mediaFilePath = stdInReader.readLine();
//            }
//            System.out.printf("%d media files entered...%n", mediaFilePaths.size());
//            mediaFilePaths.forEach(file -> {
//            	archiveMetadataService.addAlbum(file,album);
//            });
//        } catch (IOException e) {
//            LOG.error("Read from System.in failed.", e);
//            return;
//        }
//
//    }

}

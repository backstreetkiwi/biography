package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.MetadataService;

public class MainRebuildAlbumFolders {
    
    private final static Log LOG = LogFactory.getLog(MainRebuildAlbumFolders.class);

    private static MetadataService metadataService;

    private static BiographyConfig config;
    
    public static void main(String[] args) throws IOException {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);

        metadataService = springContext.getBean(MetadataService.class);
        config = springContext.getBean(BiographyConfig.class);
        
        rebuildAlbumFolders();

        springContext.close();
        System.out.println("Bye...");
    }

    public static void rebuildAlbumFolders() throws IOException {
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Existing Biography album folders will be deleted!!!");
        System.out.println("Abort program if you do not want the old album folders to be deleted!");
        System.out.println("[ENTER] to continue...");
        try {
            stdInReader.readLine();
        } catch (IOException e) {
          LOG.error("Read from System.in failed.", e);
          return;
        }

        File albumFolder = new File(config.getArchiveFolder(), "albums");
        FileUtils.deleteDirectory(albumFolder);
        FileUtils.forceMkdir(albumFolder);
        
        List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());
        
        final Map<Album,List<File>> albumContent = new HashMap<>();
        
        System.out.printf("Found %d media files.%n%n", mediaFiles.size());

        mediaFiles.stream().forEach(file -> {
            BiographyMetadata metadata = metadataService.getMetadata(file);
            if(metadata.getAlbums().isEmpty()) {
                return;
            }
            metadata.getAlbums().forEach(album -> {
                if(!albumContent.containsKey(album)) {
                    albumContent.put(album, new ArrayList<>());
                }
                albumContent.get(album).add(file);
            });
        });

        albumContent.forEach((album, list) -> {
            int year = list.stream().map(metadataService::getMetadata).map(BiographyMetadata::getDateTimeOriginal).sorted().findFirst().get().getYear();
            File thisAlbumFolder = new File(albumFolder, "" + year + "/" + album.getTitle() + "/" + album.getChapter().orElse(""));
            try {
                FileUtils.forceMkdir(thisAlbumFolder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException();
            }
            list.forEach(file -> {
                try {
                    FileUtils.copyFileToDirectory(file, thisAlbumFolder);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        });
        
    }

}

package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
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

        File albumBaseFolder = new File(config.getArchiveFolder(), "albums");
        FileUtils.deleteDirectory(albumBaseFolder);
        FileUtils.forceMkdir(albumBaseFolder);
        
        // TODO utils should give me a stream!
        List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());
        
        System.out.printf("Found %d media files.%n%n", mediaFiles.size());

        final Counter counter = new Counter(1);
        
        Function<File, Stream<Pair<File, Album>>> flatMapFileToAlbumFilePairs = file -> {
            System.out.printf("%05d -> %s%n", counter.getCounter(), file.toString());
            counter.plus1();
            return metadataService.getMetadata(file).getAlbums().stream().map(album -> Pair.of(file, album));
        };

        List<Triple<File, Album, LocalDateTime>> allFilesWithAlbumAndDateTimeOriginal = mediaFiles.stream()
                .flatMap(flatMapFileToAlbumFilePairs)
                .map(pair -> Triple.of(pair.getLeft(), pair.getRight(), metadataService.getMetadata(pair.getLeft()).getDateTimeOriginal()))
                .collect(Collectors.toList());

        Map<String, Optional<LocalDateTime>> albumStartDateByTitle = allFilesWithAlbumAndDateTimeOriginal.stream()
                .collect(Collectors.groupingBy(triple -> triple.getMiddle().getTitle(), Collectors.mapping(Triple::getRight, Collectors.minBy(LocalDateTime::compareTo))));

        Map<Integer, List<String>> albumTitlesChronologicallyGroupedByYear = albumStartDateByTitle.entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getValue().get(), entry.getKey()))
            .sorted(Comparator.comparing(Pair::getLeft))
            .collect(Collectors.groupingBy(pair -> pair.getLeft().getYear(), Collectors.mapping(Pair::getRight, Collectors.toList())));
        
        allFilesWithAlbumAndDateTimeOriginal.stream()
              .map(triple -> Triple.of(triple.getLeft(), triple.getMiddle(), albumStartDateByTitle.get(triple.getMiddle().getTitle()).get()))
              .map(triple -> Pair.of(triple.getLeft(), getAlbumFolder(albumBaseFolder, triple.getMiddle(), triple.getRight().getYear(), albumTitlesChronologicallyGroupedByYear.get(triple.getRight().getYear()).indexOf(triple.getMiddle().getTitle()))))
              .forEach(sourceFileAndtargetFolder -> {
                  try {
                      FileUtils.copyFileToDirectory(sourceFileAndtargetFolder.getLeft(), sourceFileAndtargetFolder.getRight());
                  } catch (IOException e) {
                      throw new RuntimeException(e);
                  }
              });
        
//        Map<String, List<Entry<Album, List<File>>>> collect = albumContent.entrySet()
//            .stream()
//            .map((key, value)Pair.of(left, right))
            
//            .collect(Collectors.groupingBy(entry -> entry.getKey().getTitle()));
        
        //System.out.println(yearByTitle);
        
        
//        albumContent.forEach((album, list) -> {
//            int year = list.stream().map(metadataService::getMetadata).map(BiographyMetadata::getDateTimeOriginal).sorted().findFirst().get().getYear();
//            File thisAlbumFolder = new File(albumFolder, "" + year + "/" + album.getTitle() + "/" + album.getChapter().orElse(""));
//            try {
//                FileUtils.forceMkdir(thisAlbumFolder);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                throw new RuntimeException();
//            }
//            list.forEach(file -> {
//                try {
//                    FileUtils.copyFileToDirectory(file, thisAlbumFolder);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            });
//        });
        
    }
    
    private static File getAlbumFolder(File baseFolder, Album album, Integer albumYear, Integer albumIndexInYear) {
        return new File(new File(new File(baseFolder, albumYear.toString()), String.format("%03d %s", albumIndexInYear+1, album.getTitle())), album.getChapter().orElse(""));        
    }
    
    static class Counter{
        private int counter;
        Counter(int value) {
            counter = value;
        }
        int getCounter() {
            return counter;
        }
        void plus1() {
            counter++;
        }
    }

}

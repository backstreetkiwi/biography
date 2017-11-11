package de.zaunkoenigweg.biography.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.metadata.MetadataService;

public class MainSetDatetimeOriginalOnFiles {
    
    private final static Log LOG = LogFactory.getLog(MainSetDatetimeOriginalOnFiles.class);
    
    private static MetadataService metadataService;

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        System.out.println("WARNING! This program might move files.");

        metadataService = springContext.getBean(MetadataService.class);
        
        setDatetimeOriginalOnFiles();

        springContext.close();
        System.out.println("Bye...");
    }

    public static void setDatetimeOriginalOnFiles() {
    	
    	// TODO Redesign with ArchiveService
    	
//        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
//        
//        String input = null;
//        LocalDateTime datetimeOriginal;
//        
//        System.out.print("Please enter timestamp: ");
//        try {
//            input = stdInReader.readLine();
//            if(StringUtils.isBlank(input)) {
//                System.out.println("Datetime must not be empty.");
//                return;
//            }
//            datetimeOriginal = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(input));
//        } catch (IOException e) {
//            LOG.error("Read from System.in failed.", e);
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
//                ExifData.setDescription(file, description);
//                BiographyMetadata oldMetadata = metadataService.getMetadata(file);
//                BiographyMetadata newMetadata = new BiographyMetadata(oldMetadata.getDateTimeOriginal(), description, oldMetadata.getAlbums());
//                metadataService.setMetadata(file, newMetadata);
//            });
//        } catch (IOException e) {
//            LOG.error("Read from System.in failed.", e);
//            return;
//        }
    }

}

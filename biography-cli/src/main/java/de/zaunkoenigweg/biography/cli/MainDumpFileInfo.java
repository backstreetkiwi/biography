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

import de.zaunkoenigweg.biography.metadata.ExifData;

public class MainDumpFileInfo {
    
    private final static Log LOG = LogFactory.getLog(MainDumpFileInfo.class);

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);

        dump();

        springContext.close();
        System.out.println("Bye...");
    }

    public static void dump() {
        
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        
        String input = null;
        
        try {
            System.out.println("Please enter full media file path(s): ");
            List<File> mediaFilePaths = new ArrayList<>();
            String mediaFilePath = stdInReader.readLine();
            while(StringUtils.isNotBlank(mediaFilePath)) {
                File file = new File(mediaFilePath);
                ExifData exifData = ExifData.of(file);
                System.out.printf("Description: '%s'%n", exifData.getDescription());
                System.out.printf("UserComment: '%s'%n", exifData.getUserComment());
                System.out.printf("Timestamp: '%s'%n%n%n", exifData.getDateTimeOriginal());
                System.out.println(ExifData.dumpExif(file));
                mediaFilePath = stdInReader.readLine();
            }
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
            return;
        }
    }

}

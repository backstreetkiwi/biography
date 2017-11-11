package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.zaunkoenigweg.biography.metadata.ExifData;

public class MainDumpExifData {
    
    private final static Log LOG = LogFactory.getLog(MainDumpExifData.class);

    public static void main(String[] args) {

        dump();

        System.out.println("Bye...");
    }

    public static void dump() {
        
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            System.out.println("Please enter full media file path(s): ");
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

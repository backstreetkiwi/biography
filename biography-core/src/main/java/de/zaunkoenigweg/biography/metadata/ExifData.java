package de.zaunkoenigweg.biography.metadata;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * EXIF-Data for a given image file.
 * 
 * As of now, only jpeg is supported.
 * 
 * @author Nikolaus Winter
 */
public class ExifData {

    private static final Logger LOG = LogManager.getLogger(ExifData.class);

    private final static DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss:SSS");;

    private LocalDateTime dateTimeOriginal;

	private ExifData() {
	}
	
	public static ExifData from(File file) {

		if(file==null) {
			LOG.error("Missing argument file.");
			return null;
		}
		
		if(!file.exists() || file.isDirectory()) {
			LOG.error(String.format("File '%s' does not exist or is a directory.", file.getAbsolutePath()));
			return null;
		}
		
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            LOG.error(String.format("Error reading EXIF data of %s", file.getAbsolutePath()));
            LOG.error(e);
            return null;
        }
		ExifData exifData = new ExifData();
		
		exifData.dateTimeOriginal = readDateTimeOriginal(metadata, file); 
		
		return exifData;
	}
	
    private static LocalDateTime readDateTimeOriginal(Metadata metadata, File file) {
        LocalDateTime dateTimeOriginal = null;
        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", file.getAbsolutePath()));
            return null;
        }
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifSubIFDDirectory == null) {
            LOG.trace(String.format("File %s: exifSubIFDDirectory is null", file.getAbsolutePath()));
            LOG.trace("exifSubIFDDirectory is null");
            return null;
        }
        Object objectDateTimeOriginal = exifSubIFDDirectory.getObject(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        if (objectDateTimeOriginal == null) {
            LOG.trace(String.format("File %s: exifSubIFDDirectory has no TAG_DATETIME_ORIGINAL", file.getAbsolutePath()));
            return null;
        }
        if (!(objectDateTimeOriginal instanceof String)) {
            LOG.trace(String.format("File %s: TAG_DATETIME_ORIGINAL does not contain a string", file.getAbsolutePath()));
            return null;
        }
        Object objectSubsecondTimeOriginal = exifSubIFDDirectory.getObject(ExifSubIFDDirectory.TAG_SUBSECOND_TIME_ORIGINAL);
        if (!(objectSubsecondTimeOriginal instanceof String)) {
            LOG.trace(String.format("File %s: TAG_SUBSECOND_TIME_ORIGINAL does not contain a string", file.getAbsolutePath()));
            objectSubsecondTimeOriginal = "000";
        }
        String dateString = String.format("%s:%03d", objectDateTimeOriginal, Integer.parseInt((String) objectSubsecondTimeOriginal));
        try {
            dateTimeOriginal = LocalDateTime.parse(dateString, EXIF_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            LOG.trace(String.format("Date '%s' could not be parsed.", objectDateTimeOriginal));
        }
        return dateTimeOriginal;
    }
	
    private static void dumpExif(Metadata metadata) {
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                LOG.trace(String.format("[%s] - %s = %s", directory.getName(), tag.getTagName(), tag.getDescription()));
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    LOG.error(String.format("ERROR: %s", error));
                }
            }
        }
    }
	
	public LocalDateTime getDateTimeOriginal() {
		return dateTimeOriginal;
	}
	
}

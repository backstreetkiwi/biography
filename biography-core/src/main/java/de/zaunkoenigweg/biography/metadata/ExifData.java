package de.zaunkoenigweg.biography.metadata;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.zaunkoenigweg.biography.core.MediaFileType;


/**
 * EXIF-Data for a given image file.
 * 
 * As of now, only jpeg is supported.
 * 
 * @author Nikolaus Winter
 */
public class ExifData {

    private final static Log LOG = LogFactory.getLog(ExifData.class);

    private static final DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss:SSS");

    private LocalDateTime dateTimeOriginal;
    private Optional<String> description;

	private ExifData() {
	}
	
	public static ExifData of(File file) {

		if(file==null) {
			LOG.trace("Missing argument 'file'.");
			return null;
		}
		
		if(!file.exists() || file.isDirectory()) {
		    LOG.trace(String.format("File '%s' does not exist or is a directory.", file.getAbsolutePath()));
			return null;
		}
		
        ImageMetadata metadata;
        try {
            metadata = Imaging.getMetadata(file);
        } catch (ImageReadException | IOException e) {
            LOG.trace(String.format("Error reading EXIF data of %s", file.getAbsolutePath()));
            LOG.trace(e);
            return null;
        }

        if (!(metadata instanceof JpegImageMetadata)) {
            LOG.trace(String.format("Error reading EXIF data of %s", file.getAbsolutePath()));
            return null;
        }
        
        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        
		ExifData exifData = new ExifData();
		
		exifData.dateTimeOriginal = readDateTimeOriginal(jpegMetadata, file);
		exifData.description = Optional.ofNullable(readDescription(jpegMetadata, file));
		
		return exifData;
	}
	
	public static boolean supports(MediaFileType mediaFileType) {
	    return MediaFileType.JPEG==mediaFileType;
	}
	
    private static LocalDateTime readDateTimeOriginal(JpegImageMetadata metadata, File file) {

        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", file.getAbsolutePath()));
            return null;
        }
        
        TiffField dateTimeOriginal = metadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);

        if (dateTimeOriginal == null) {
            LOG.trace(String.format("File %s has no EXIF_TAG_DATE_TIME_ORIGINAL", file.getAbsolutePath()));
            return null;
        }
        
        String dateTimeOriginalValue = null;
        try {
            dateTimeOriginalValue = dateTimeOriginal.getStringValue();
        } catch (ImageReadException e) {
            LOG.trace(String.format("File %s: Error whilereading EXIF_TAG_DATE_TIME_ORIGINAL", file.getAbsolutePath()));
            return null;
        }
        
        TiffField subsecondTimeOriginal = metadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL);

        String subsecondOriginalValue = "000";

        if (subsecondTimeOriginal != null) {
            try {
                subsecondOriginalValue = subsecondTimeOriginal.getStringValue();
            } catch (ImageReadException e) {
                // stays '000', which is fine...
                LOG.trace(String.format("File %s: Error whilereading EXIF_TAG_SUB_SEC_TIME_ORIGINAL", file.getAbsolutePath()));
            }
        }
        
        String dateString = String.format("%s:%03d", dateTimeOriginalValue, Integer.parseInt(subsecondOriginalValue));
        try {
            return LocalDateTime.parse(dateString, EXIF_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            LOG.trace(String.format("Date '%s' could not be parsed.", dateString));
            return null;
        }
    }
    
    private static String readDescription(JpegImageMetadata metadata, File file) {
        
        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", file.getAbsolutePath()));
            return null;
        }
        
        TiffField imageDescription = metadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);

        if (imageDescription == null) {
            LOG.trace(String.format("File %s has no TIFF_TAG_IMAGE_DESCRIPTION", file.getAbsolutePath()));
            return null;
        }
        
        try {
            byte[] descriptionRaw = imageDescription.getByteArrayValue();
            int nullPosition = ArrayUtils.indexOf(descriptionRaw, (byte)0);
            if(nullPosition!=-1) {
                descriptionRaw = ArrayUtils.subarray(descriptionRaw, 0, nullPosition);
            }
            return new String(descriptionRaw, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public LocalDateTime getDateTimeOriginal() {
		return dateTimeOriginal;
	}

    public Optional<String> getDescription() {
        return description;
    }
}

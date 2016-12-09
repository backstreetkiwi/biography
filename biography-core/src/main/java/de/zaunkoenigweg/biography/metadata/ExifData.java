package de.zaunkoenigweg.biography.metadata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
    private static byte[] UTF8_PREFIX = Arrays.copyOf("UTF-8".getBytes(), 8);

    private LocalDateTime dateTimeOriginal;
    private Optional<String> description;
    private Optional<String> userComment;

    private ExifData() {
    }

    public static ExifData of(File file) {

        if (file == null) {
            LOG.trace("Missing argument 'file'.");
            return null;
        }

        if (!file.exists() || file.isDirectory()) {
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
        exifData.userComment = Optional.ofNullable(readUserComment(jpegMetadata, file));

        return exifData;
    }

    public static boolean supports(MediaFileType mediaFileType) {
        return MediaFileType.JPEG == mediaFileType;
    }

    public static void setDescription(File file, String description) {

        if (StringUtils.isBlank(description)) {
            return;
        }
        
        byte[] descriptionBytes;
        try {
            descriptionBytes = description.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            LOG.trace("Encoding ISO-8859-1 not supported!?");
            return;
        }
        setField(file, TiffOutputSet::getOrCreateRootDirectory, TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, FieldType.ASCII, descriptionBytes);
    }
    
    public static void setUserComment(File file, String userComment) {
        if (StringUtils.isBlank(userComment)) {
            return;
        }
        
        byte[] userCommentBytesWithoutPrefix;
        try {
            userCommentBytesWithoutPrefix = userComment.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.trace("Encoding UTF-8 not supported!?");
            return;
        }
        byte[] userCommentBytes = new byte[UTF8_PREFIX.length + userCommentBytesWithoutPrefix.length];
        System.arraycopy(UTF8_PREFIX, 0, userCommentBytes, 0, UTF8_PREFIX.length);
        System.arraycopy(userCommentBytesWithoutPrefix, 0, userCommentBytes, UTF8_PREFIX.length, userCommentBytesWithoutPrefix.length);
        
        
        setField(file, TiffOutputSet::getOrCreateExifDirectory, ExifTagConstants.EXIF_TAG_USER_COMMENT, FieldType.UNDEFINED, userCommentBytes);
    }

    private static void setField(File file, GetOrCreateOutputDirectory getOrCreateOutputDirectory, TagInfo tagInfo, FieldType fieldType, byte[] content) {

        try {
            
            byte[] sourceFileBytes = Files.readAllBytes(file.toPath());
            final ImageMetadata metadata = Imaging.getMetadata(sourceFileBytes);
            if(!(metadata instanceof JpegImageMetadata)) {
                LOG.trace(String.format("Image %s has no JPEG metadata", file.getAbsolutePath()));
                return;
            }
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            final TiffImageMetadata exif = jpegMetadata.getExif();
            
            TiffOutputSet tiffOutputSet = null;
            
            if(exif != null) {
                tiffOutputSet = exif.getOutputSet();
            }
            if(tiffOutputSet == null) {
                tiffOutputSet = new TiffOutputSet();
            }
            final TiffOutputDirectory exifDirectory = getOrCreateOutputDirectory.getOrCreate(tiffOutputSet);
            exifDirectory.removeField(tagInfo);
            exifDirectory.add(new TiffOutputField(tagInfo, fieldType, content.length, content));
            
            new ExifRewriter().updateExifMetadataLossless(sourceFileBytes, new BufferedOutputStream(new FileOutputStream(file)), tiffOutputSet);
            
        } catch (IOException | ImageReadException | ImageWriteException e) {
            LOG.trace(String.format("Error writing description to %s", file.getAbsolutePath()));
            LOG.trace(e);
        }
    }
    
    @FunctionalInterface
    private interface GetOrCreateOutputDirectory {
        public TiffOutputDirectory getOrCreate(TiffOutputSet tiffOutputSet) throws ImageWriteException;
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
            int nullPosition = ArrayUtils.indexOf(descriptionRaw, (byte) 0);
            if (nullPosition != -1) {
                descriptionRaw = ArrayUtils.subarray(descriptionRaw, 0, nullPosition);
            }
            return new String(descriptionRaw, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String readUserComment(JpegImageMetadata metadata, File file) {

        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", file.getAbsolutePath()));
            return null;
        }

        TiffField userComment = metadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_USER_COMMENT);

        if (userComment == null) {
            LOG.trace(String.format("File %s has no EXIF_TAG_USER_COMMENT", file.getAbsolutePath()));
            return null;
        }

        try {
            byte[] userCommentRaw = userComment.getByteArrayValue();
            if(userCommentRaw.length < UTF8_PREFIX.length) {
                LOG.trace(String.format("File %s: Field EXIF_TAG_USER_COMMENT is too short.", file.getAbsolutePath()));
                return null;
            }
            byte[] prefix = new byte[UTF8_PREFIX.length]; 
            System.arraycopy(userCommentRaw, 0, prefix, 0, UTF8_PREFIX.length);
            if(!Arrays.equals(UTF8_PREFIX, prefix)) {
                LOG.trace(String.format("File %s: Field EXIF_TAG_USER_COMMENT does not contain UTF-8 text.", file.getAbsolutePath()));
                return null;
            }
            byte[] contentRaw = new byte[userCommentRaw.length-UTF8_PREFIX.length];
            System.arraycopy(userCommentRaw, 8, contentRaw, 0, contentRaw.length);
            return new String(contentRaw, "UTF-8");
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

    public Optional<String> getUserComment() {
        return userComment;
    }

}

package de.zaunkoenigweg.biography.metadata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
 * As of now, only JPEG is supported.
 * 
 * @author Nikolaus Winter
 */
public class ExifData {

    private final static Log LOG = LogFactory.getLog(ExifData.class);

    private static final DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss:SSS");
    private static byte[] UNICODE_PREFIX = Arrays.copyOf("UNICODE".getBytes(), 8);

    private LocalDateTime dateTimeOriginal;
    private Optional<String> description;
    private Optional<String> userComment;
    private Optional<String> cameraModel;
    
    private ExifData() {
    }

    /**
     * Create EXIF data object from file.
     * 
     * Uses UTF-8 as the standard encoding.
     * 
     * @param file image file
     * @return EXIF data object
     */
    public static ExifData of(File file) {
        return of(file, StandardCharsets.UTF_8);
    }

    /**
     * Create EXIF data object from file.
     * 
     * If you want to read 'legacy' files which do not use UTF-8 to encode the description,
     * you can provide the encoding (e.g. {@link StandardCharsets#ISO_8859_1}). 
     * 
     * @param file image file
     * @param readDescriptionCharset charset to use for reading the description.
     * @return EXIF data object
     */
    public static ExifData of(File file, Charset readDescriptionCharset) {

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
        exifData.description = Optional.ofNullable(readDescription(jpegMetadata, file, readDescriptionCharset));
        exifData.userComment = Optional.ofNullable(readUserComment(jpegMetadata, file));
        exifData.cameraModel = Optional.ofNullable(readCameraModel(jpegMetadata, file));

        return exifData;
    }

    /**
     * Does this class support the given media file type?
     * @param mediaFileType media file type
     * @return
     */
    public static boolean supports(MediaFileType mediaFileType) {
        return MediaFileType.JPEG == mediaFileType;
    }

    /**
     * Sets the description of the given image file.
     * The description is stored in UTF-8 encoding and the file is immediately written to disk losslessly. 
     * 
     * @param file image file
     * @param description new description
     */
    public static void setDescription(File file, String description) {

    	String descriptionToWriteToExif = "";
    	
        if (StringUtils.isNotBlank(description)) {
            descriptionToWriteToExif = description;
        }
        
        byte[] descriptionBytes = descriptionToWriteToExif.getBytes(StandardCharsets.UTF_8);
        setField(file, TiffOutputSet::getOrCreateRootDirectory, TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, FieldType.ASCII, descriptionBytes);
    }
    
    /**
     * Sets the EXIF field DATETIME_ORIGINAL to the given timestamp in the given image file.
     * 
     * As the timestamp can have a millisecond information, the value is split into two EXIF fields:
     * DATETIME_ORIGINAL and SUBSECONDS_TIME_ORIGINAL.
     * 
     * @param file image file
     * @param dateTimeOriginal new DATETIME_ORIGINAL
     */
    public static void setDateTimeOriginal(File file, LocalDateTime dateTimeOriginal) {

        if (dateTimeOriginal==null) {
            return;
        }
        
        String dateTimeOriginalText = EXIF_DATE_TIME_FORMATTER.format(dateTimeOriginal);
        byte[] exifDateTimeOriginal = StringUtils.substringBeforeLast(dateTimeOriginalText, ":").getBytes(StandardCharsets.US_ASCII);
        byte[] exifSubSecTimeOriginal = StringUtils.substringAfterLast(dateTimeOriginalText, ":").getBytes(StandardCharsets.US_ASCII);

        // TODO two subsequent file writes seem to be not the best idea. Builder pattern or the like available?
        setField(file, TiffOutputSet::getOrCreateExifDirectory, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, FieldType.ASCII, exifDateTimeOriginal);
        setField(file, TiffOutputSet::getOrCreateExifDirectory, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL, FieldType.ASCII, exifSubSecTimeOriginal);
    }
    
    /**
     * Sets the EXIF field USER_COMMENT in the given image file.
     * 
     * @param file image file
     * @param description new description
     */
    public static void setUserComment(File file, String userComment) {
        if (StringUtils.isBlank(userComment)) {
            return;
        }
        
        byte[] userCommentBytesWithoutPrefix;
        userCommentBytesWithoutPrefix = userComment.getBytes(StandardCharsets.UTF_16);
        byte[] userCommentBytes = new byte[UNICODE_PREFIX.length + userCommentBytesWithoutPrefix.length];
        System.arraycopy(UNICODE_PREFIX, 0, userCommentBytes, 0, UNICODE_PREFIX.length);
        System.arraycopy(userCommentBytesWithoutPrefix, 0, userCommentBytes, UNICODE_PREFIX.length, userCommentBytesWithoutPrefix.length);
        
        setField(file, TiffOutputSet::getOrCreateExifDirectory, ExifTagConstants.EXIF_TAG_USER_COMMENT, FieldType.UNDEFINED, userCommentBytes);
    }

    /**
     * Generic method to set various fields in EXIF data.
     * 
     * The file is written to disk losslessly immediately.
     * 
     * @param file image file
     * @param getOrCreateOutputDirectory method that provides the EXIF directory
     * @param tagInfo EXIF tag of the field (can be considered the 'key')
     * @param fieldType field type (e.g. ASCII, ...)
     * @param content content as byte array
     */
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
            LOG.trace(String.format("Error writing %s to %s", tagInfo.name, file.getAbsolutePath()));
            LOG.trace(e);
        }
    }
    
    /**
     * type of the method that maps from TiffOutputSet to EXIF directory
     * 
     * {@link ExifData#setField(File, GetOrCreateOutputDirectory, TagInfo, FieldType, byte[])}
     */
    @FunctionalInterface
    private interface GetOrCreateOutputDirectory {
        public TiffOutputDirectory getOrCreate(TiffOutputSet tiffOutputSet) throws ImageWriteException;
    }

    /**
     * Reads DATETIME_ORIGINAL from given EXIF metadata
     * 
     * @param metadata image metadata
     * @param file original file, only used for logging purposes
     * @return DATETIME_ORIGINAL
     */
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

    /**
     * Reads DESCRIPTION from given EXIF metadata
     * 
     * @param metadata image metadata
     * @param file original file, only used for logging purposes
     * @param charset used charset, usually {@link StandardCharsets#UTF_8}
     * @return DESCRIPTION
     */
    private static String readDescription(JpegImageMetadata metadata, File file, Charset charset) {

        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", file.getAbsolutePath()));
            return null;
        }

        TiffField imageDescription = metadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);

        if (imageDescription == null) {
            LOG.trace(String.format("File %s has no TIFF_TAG_IMAGE_DESCRIPTION", file.getAbsolutePath()));
            return null;
        }

        byte[] descriptionRaw = imageDescription.getByteArrayValue();
        int nullPosition = ArrayUtils.indexOf(descriptionRaw, (byte) 0);
        if (nullPosition != -1) {
            descriptionRaw = ArrayUtils.subarray(descriptionRaw, 0, nullPosition);
        }
        return new String(descriptionRaw, charset);
    }

    /**
     * Reads camera model from given EXIF metadata
     * 
     * @param metadata image metadata
     * @param file original file, only used for logging purposes
     * @return Camera make and model
     */
    private static String readCameraModel(JpegImageMetadata metadata, File file) {

        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", file.getAbsolutePath()));
            return null;
        }

        TiffField cameraModel = metadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_MODEL);

        if (cameraModel == null) {
            LOG.trace(String.format("File %s has no TIFF_TAG_MODEL", file.getAbsolutePath()));
            return null;
        }

        byte[] modelRaw = cameraModel.getByteArrayValue();
        int nullPosition = ArrayUtils.indexOf(modelRaw, (byte) 0);
        if (nullPosition != -1) {
            modelRaw = ArrayUtils.subarray(modelRaw, 0, nullPosition);
        }
        return new String(modelRaw, StandardCharsets.ISO_8859_1);
    }

    /**
     * Reads USER_COMMENT from given EXIF metadata
     * 
     * @param metadata image metadata
     * @param file original file, only used for logging purposes
     * @return USER_COMMENT
     */
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

        byte[] userCommentRaw = userComment.getByteArrayValue();
        if(userCommentRaw.length < UNICODE_PREFIX.length) {
            LOG.trace(String.format("File %s: Field EXIF_TAG_USER_COMMENT is too short.", file.getAbsolutePath()));
            return null;
        }
        byte[] prefix = new byte[UNICODE_PREFIX.length]; 
        System.arraycopy(userCommentRaw, 0, prefix, 0, UNICODE_PREFIX.length);
        if(!Arrays.equals(UNICODE_PREFIX, prefix)) {
            LOG.trace(String.format("File %s: Field EXIF_TAG_USER_COMMENT does not contain Unicode text.", file.getAbsolutePath()));
            return null;
        }
        byte[] contentRaw = new byte[userCommentRaw.length-UNICODE_PREFIX.length];
        System.arraycopy(userCommentRaw, 8, contentRaw, 0, contentRaw.length);
        return new String(contentRaw, StandardCharsets.UTF_16);
    }

    public LocalDateTime getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getCameraModel() {
        return cameraModel;
    }

    public Optional<String> getUserComment() {
        return userComment;
    }

    /**
     * Dumps the EXIF data of the given file to a string.
     * @param file image file
     * @return all EXIF data
     */
    public static String dumpExif(File file) {

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

        return jpegMetadata.toString();
    }

}

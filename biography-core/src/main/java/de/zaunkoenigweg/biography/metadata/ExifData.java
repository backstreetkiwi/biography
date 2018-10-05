package de.zaunkoenigweg.biography.metadata;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.exif.Exif;
import de.zaunkoenigweg.biography.metadata.exif.Exiftool;

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

  private LocalDateTime dateTimeOriginal;
  private Optional<String> description;
  private Optional<String> userComment;
  private Optional<String> cameraModel;

  private ExifData() {
  }

  /**
   * Create EXIF data object from file.
   * 
   * @param file
   *          image file
   * @return EXIF data object
   */
  public static ExifData of(File file) {

    if (file == null) {
      LOG.trace("Missing argument 'file'.");
      return null;
    }

    if (!file.exists() || file.isDirectory()) {
      LOG.trace(String.format("File '%s' does not exist or is a directory.", file.getAbsolutePath()));
      return null;
    }

    HashSet<Exif> exifFields = Sets.newHashSet(Exif.DATETIME_ORIGINAL, Exif.SUBSEC_TIME_ORIGINAL,
        Exif.IMAGE_DESCRIPTION, Exif.USER_COMMENT, Exif.CAMERA_MODEL);

    Map<Exif, String> rawExifData = null;
    try {
      rawExifData = Exiftool.read(file, exifFields);
    } catch (IllegalStateException e) {
      return null;
    }
    
    ExifData exifData = new ExifData();

    exifData.dateTimeOriginal = toDateTimeOriginal(rawExifData.get(Exif.DATETIME_ORIGINAL), rawExifData.get(Exif.SUBSEC_TIME_ORIGINAL));
    exifData.description = Optional.ofNullable(rawExifData.get(Exif.IMAGE_DESCRIPTION));
    exifData.userComment = Optional.ofNullable(rawExifData.get(Exif.USER_COMMENT));
    exifData.cameraModel = Optional.ofNullable(rawExifData.get(Exif.CAMERA_MODEL));

    return exifData;
  }

  /**
   * Does this class support the given media file type?
   * 
   * @param mediaFileType
   *          media file type
   * @return
   */
  public static boolean supports(MediaFileType mediaFileType) {
    return MediaFileType.JPEG == mediaFileType;
  }

  /**
   * Sets the description of the given image file. The description is stored in
   * UTF-8 encoding and the file is immediately written to disk losslessly.
   * 
   * @param file
   *          image file
   * @param description
   *          new description
   */
  public static void setDescription(File file, String description) {

    String descriptionToWriteToExif = "";

    if (StringUtils.isNotBlank(description)) {
      descriptionToWriteToExif = description;
    }

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.IMAGE_DESCRIPTION, descriptionToWriteToExif);
    
    Exiftool.write(file, values);
  }

  /**
   * Sets the EXIF field DATETIME_ORIGINAL to the given timestamp in the given
   * image file.
   * 
   * As the timestamp can have a millisecond information, the value is split into
   * two EXIF fields: DATETIME_ORIGINAL and SUBSECONDS_TIME_ORIGINAL.
   * 
   * @param file
   *          image file
   * @param dateTimeOriginal
   *          new DATETIME_ORIGINAL
   */
  public static void setDateTimeOriginal(File file, LocalDateTime dateTimeOriginal) {

    if (dateTimeOriginal == null) {
      return;
    }

    dateTimeOriginal = dateTimeOriginal.truncatedTo(ChronoUnit.MILLIS);

    String dateTimeOriginalText = EXIF_DATE_TIME_FORMATTER.format(dateTimeOriginal);
    String exifDateTimeOriginal = StringUtils.substringBeforeLast(dateTimeOriginalText, ":");
    String exifSubSecTimeOriginal = StringUtils.substringAfterLast(dateTimeOriginalText, ":");

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.DATETIME_ORIGINAL, exifDateTimeOriginal);
    values.put(Exif.SUBSEC_TIME_ORIGINAL, exifSubSecTimeOriginal);
    
    Exiftool.write(file, values);
  }

  /**
   * Sets the EXIF field USER_COMMENT in the given image file.
   * 
   * @param file
   *          image file
   * @param description
   *          new description
   */
  public static void setUserComment(File file, String userComment) {
    if (StringUtils.isBlank(userComment)) {
      return;
    }

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.USER_COMMENT, userComment);
    
    Exiftool.write(file, values);
  }

  /**
   * Create Date/Time Original from two fields in EXIF.
   * @param datetimeOriginal
   * @param subsecTimeOriginal
   * @return
   */
  private static LocalDateTime toDateTimeOriginal(String datetimeOriginal, String subsecTimeOriginal) {

    Integer subseconds = 0;
    
    if(StringUtils.isNotBlank(subsecTimeOriginal)) {
      if(StringUtils.isNumeric(subsecTimeOriginal)) {
        if(StringUtils.length(subsecTimeOriginal)<=3) {
          subseconds = Integer.parseInt(subsecTimeOriginal);
        }
      }
    }

    String dateString = String.format("%s:%03d", datetimeOriginal, subseconds);
    try {
      return LocalDateTime.parse(dateString, EXIF_DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      LOG.trace(String.format("Date '%s' could not be parsed.", dateString));
      return null;
    }
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

}

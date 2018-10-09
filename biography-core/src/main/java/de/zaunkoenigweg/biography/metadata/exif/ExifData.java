package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

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

    public static final Set<Exif> EXIF_FIELDS = Sets.immutableEnumSet(Exif.DATETIME_ORIGINAL, Exif.SUBSEC_TIME_ORIGINAL, Exif.IMAGE_DESCRIPTION, Exif.USER_COMMENT, Exif.CAMERA_MODEL);
    
    private LocalDateTime dateTimeOriginal;
    private Optional<String> description;
    private Optional<String> userComment;
    private Optional<String> cameraModel;

    /**
     * Create EXIF data object from raw EXIF data.
     * 
     * The raw EXIF data must at least contain the field {@link Exif#DATETIME_ORIGINAL}.
     * 
     * @param file image file name, must not be null
     * @param rawExifData raw data as retrieved by {@link Exiftool#read(File, java.util.Set)}, must not be null
     * 
     * @return EXIF data object
     * 
     * @throws IllegalArgumentException ... if one of the arguments is missing
     * @throws IllegalStateException ... if the given <code>rawExifData</code> is lacking essential data.
     */
    public ExifData(Map<Exif, String> rawExifData) {
        
        if (rawExifData == null) {
            throw new IllegalArgumentException("Parameter 'rawExifData' is missing.");
        }
        
        this.dateTimeOriginal = toDateTimeOriginal(rawExifData.get(Exif.DATETIME_ORIGINAL), rawExifData.get(Exif.SUBSEC_TIME_ORIGINAL));
        
        if(this.dateTimeOriginal==null) {
            throw new IllegalStateException("The raw EXIF data does not contain a valid field 'Date/Time Original'");
        }
        
        this.description = Optional.ofNullable(rawExifData.get(Exif.IMAGE_DESCRIPTION));
        this.userComment = Optional.ofNullable(rawExifData.get(Exif.USER_COMMENT));
        this.cameraModel = Optional.ofNullable(rawExifData.get(Exif.CAMERA_MODEL));
    }
    
    public ExifData(LocalDateTime dateTimeOriginal) {
        if(dateTimeOriginal==null) {
            throw new IllegalArgumentException("Date/Time Original must not be null");
        }
        this.dateTimeOriginal = dateTimeOriginal;
        this.description = Optional.empty();
        this.userComment = Optional.empty();
        this.cameraModel = Optional.empty();
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
            subseconds = Integer.max(0, Integer.parseInt(subsecTimeOriginal));
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
    
    public Map<Exif, String> getDateTimeOriginalExifFields() {
        if (dateTimeOriginal == null) {
            return null;
          }

          LocalDateTime dateTimeOriginalWithMillisPrecision = dateTimeOriginal.truncatedTo(ChronoUnit.MILLIS);

          String dateTimeOriginalText = EXIF_DATE_TIME_FORMATTER.format(dateTimeOriginalWithMillisPrecision);
          String exifDateTimeOriginal = StringUtils.substringBeforeLast(dateTimeOriginalText, ":");
          String exifSubSecTimeOriginal = StringUtils.substringAfterLast(dateTimeOriginalText, ":");

          Map<Exif, String> values = new HashMap<>();
          values.put(Exif.DATETIME_ORIGINAL, exifDateTimeOriginal);
          values.put(Exif.SUBSEC_TIME_ORIGINAL, exifSubSecTimeOriginal);
          return values;
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

    public Optional<String> getCameraModel() {
        return cameraModel;
    }
    
    public void setDateTimeOriginal(LocalDateTime dateTimeOriginal) {
        if(dateTimeOriginal==null) {
            throw new IllegalArgumentException("Date/Time Original must not be null");
        }
        this.dateTimeOriginal = dateTimeOriginal;
    }

    public void setDescription(String description) {
        this.description = Optional.ofNullable(description);
    }

    public void setUserComment(String userComment) {
        this.userComment = Optional.ofNullable(userComment);
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = Optional.ofNullable(cameraModel);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cameraModel == null) ? 0 : cameraModel.hashCode());
        result = prime * result + ((dateTimeOriginal == null) ? 0 : dateTimeOriginal.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((userComment == null) ? 0 : userComment.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExifData other = (ExifData) obj;
        if (cameraModel == null) {
            if (other.cameraModel != null)
                return false;
        } else if (!cameraModel.equals(other.cameraModel))
            return false;
        if (dateTimeOriginal == null) {
            if (other.dateTimeOriginal != null)
                return false;
        } else if (!dateTimeOriginal.equals(other.dateTimeOriginal))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (userComment == null) {
            if (other.userComment != null)
                return false;
        } else if (!userComment.equals(other.userComment))
            return false;
        return true;
    }
    
}

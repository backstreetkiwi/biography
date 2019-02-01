package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

import de.zaunkoenigweg.lexi4j.exiftool.ExifData;
import de.zaunkoenigweg.lexi4j.exiftool.Exiftool;

/**
 * EXIF-Data (wraps {@link ExifData}).
 * 
 * @author Nikolaus Winter
 */
public class ExifDataWrapper {

    private LocalDateTime dateTimeOriginal;
    private Optional<String> description;
    private Optional<String> userComment;

    /**
     * Create EXIF data object from raw EXIF data.
     * 
     * The raw EXIF data must at least contain the field {@link Exif#DATETIME_ORIGINAL}.
     * 
     * @param exifData raw data as retrieved by {@link Exiftool#read(File, java.util.Set)}, must not be null
     * 
     * @return EXIF data object
     * 
     * @throws IllegalArgumentException ... if no raw EXIF data is set or the date/time original is missing.
     */
    public ExifDataWrapper(ExifData exifData) {
        
        if (exifData == null) {
            throw new IllegalArgumentException("Parameter 'exifData' is missing.");
        }
        
        if(!exifData.getDateTimeOriginal().isPresent()) {
            throw new IllegalArgumentException("The raw EXIF data does not contain a valid field 'Date/Time Original'");
        }
        
        this.dateTimeOriginal = exifData.getDateTimeOriginal().get().withNano(exifData.getSubsecTimeOriginal().orElse(0) * 1_000_000);
        
        this.description = exifData.getImageDescription();
        this.userComment = exifData.getUserComment();
    }
    
    public ExifDataWrapper(LocalDateTime dateTimeOriginal) {
        if(dateTimeOriginal==null) {
            throw new IllegalArgumentException("Date/Time Original must not be null");
        }
        this.dateTimeOriginal = dateTimeOriginal;
        this.description = Optional.empty();
        this.userComment = Optional.empty();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ExifDataWrapper other = (ExifDataWrapper) obj;
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

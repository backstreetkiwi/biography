package de.zaunkoenigweg.biography.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class MetadataService {

    private final static Log LOG = LogFactory.getLog(MetadataService.class);

    public MetadataService() {
		LOG.info("MetadataService started.");
	}

	/**
     * Writes metadata into EXIF data of a file.
     * 
     * The metadata is exported into a JSON string which is written in the USER_COMMENT field in the EXIF data.
     * 
     * The EXIF fields DESCRIPTION and DATETIME_ORIGINAL are written accordingly in order to have a consistent media file.
     * 
     * @param file File to write the Metatada into (as EXIF).
     * @param metadata BiographyMetadata
     */
    public void writeMetadataIntoExif(File file, BiographyMetadata metadata) {
        // TODO Perhaps use some kind of builder pattern to avoid changing the whole file three times
        ExifData.setUserComment(file, metadata.toJson());
        ExifData.setDescription(file, metadata.getDescription());
        ExifData.setDateTimeOriginal(file, metadata.getDateTimeOriginal());
    }

    /**
     * Reads metadata from EXIF data of a file.
     * 
     * The metadata is read as a JSON string from the USER_COMMENT field of the EXIF data.
     * 
     * This method does not check if the other EXIF fields are consistent to the metadata. 
     * Use {@link #isExifDataConsistentToMetadata(File, BiographyMetadata)} to test consistency!
     * 
     * @param file media file with EXIF data
     * @return Biography metadata
     */
    public BiographyMetadata readMetadataFromExif(File file) {
        ExifData exifData = ExifData.of(file);
        if(exifData==null) {
            return null;
        }
        Optional<String> userComment = exifData.getUserComment();
        if(!userComment.isPresent()) {
            return null;
        }
        return BiographyMetadata.from(userComment.get());
    }
    
    /**
     * Checks if the EXIF data is consistent to the Biography metadata
     * 
     * The fields that are checked are DESCRIPTION and DATETIME_ORIGINAL.
     * 
     * @param file media file with EXIF data
     * @param metadata BiographyMetadata
     * @return Is the EXIF data consistent to the Biography metadata?
     */
    public boolean isExifDataConsistentToMetadata(File file, BiographyMetadata metadata) {
        ExifData exifData = ExifData.of(file);
        if(!exifData.getDateTimeOriginal().equals(metadata.getDateTimeOriginal())) {
            return false;
        }
        if(!StringUtils.equals(metadata.getDescription(), exifData.getDescription().orElse(null))) {
            return false;
        }
        return true;
    }

    public void writeMetadataToJsonFile(File jsonFile, BiographyMetadata metadata) {
        try {
            FileUtils.write(jsonFile, metadata.toJson(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error writing metadata JSON file.", e);
            return;
        }
    }

    public BiographyMetadata readMetadataFromJsonFile(File jsonFile) {
        if(!jsonFile.exists() || !jsonFile.isFile()) {
            return null;
        }
        try {
            return BiographyMetadata.from(FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.error("Error reading metadata JSON file.", e);
            return null;
        }
    }

}

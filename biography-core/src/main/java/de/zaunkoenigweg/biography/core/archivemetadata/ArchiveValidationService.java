package de.zaunkoenigweg.biography.core.archivemetadata;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileName;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.MetadataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifData;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;

/**
 * Validate archived media files.
 */
@Component
public class ArchiveValidationService {

    private final static Log LOG = LogFactory.getLog(ArchiveValidationService.class);

    private ExifDataService exifDataService;
    private MetadataService metadataService;
    private File archiveFolder;

    public ArchiveValidationService(MetadataService metadataService, ExifDataService exifDataService, File archiveFolder) {
        this.metadataService = metadataService;
        this.exifDataService = exifDataService;
        this.archiveFolder = archiveFolder;
        LOG.info("ArchiveValidationService started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
    }

    /**
     * Is the archive file valid?
     * 
     * @param file Biography media file, must not be {@code null}
     * @return Is the archive file valid?
     */
    public boolean isValid(File file) {
    	return ValidationResult.OK == validate(file);
    }

    /**
     * Validate Biography media file in archive.
     * 
     * @param file Biography media file, must not be {@code null}
     * @return validation result
     */
    public ValidationResult validate(File file) {
    	
    	Objects.requireNonNull(file, "The archive file must not be null.");

    	if(!file.exists() || file.isDirectory()) {
    		return ValidationResult.FILE_DOES_NOT_EXIST;
    	}
    	
    	if(!MediaFileName.isValid(file.getName())) {
    		return ValidationResult.FILENAME_NOT_VALID;
    	}
    	
    	MediaFileName mediaFileName = MediaFileName.of(file.getName());
    	
    	if(!file.equals(mediaFileName.archiveFile(archiveFolder))) {
    		return ValidationResult.FILE_IS_NOT_IN_CORRECT_ARCHIVE_FILDER;
    	}
    	
        BiographyMetadata metadata;

        if (ExifDataService.supports(mediaFileName.getType())) {
            metadata = metadataService.readMetadataFromExif(file);
        } else {
            metadata = metadataService.readMetadataFromJsonFile(getMetadataJsonFile(file, mediaFileName));
        }

        if(metadata==null) {
        	return ValidationResult.FILE_HAS_NO_METADATA;
        }
    	
        if(!mediaFileName.getDateTimeOriginal().equals(metadata.getDateTimeOriginal().truncatedTo(ChronoUnit.SECONDS))) {
        	return ValidationResult.DATETIME_ORIGINAL_INCONSISTENT;
        }
    	
        if (ExifDataService.supports(mediaFileName.getType())) {
        	ExifData exifData = exifDataService.readExifData(file);
        	
        	if (!metadata.getDateTimeOriginal().equals(exifData.getDateTimeOriginal())) {
        		return ValidationResult.METADATA_INCONSISTENT;
        	}
        	
        	String metadataDescription = StringUtils.trimToEmpty(metadata.getDescription());
        	String exifDataDescription = StringUtils.trimToEmpty(exifData.getDescription().orElse(""));
        	
        	if (!metadataDescription.equals(exifDataDescription)) {
        		return ValidationResult.METADATA_INCONSISTENT;
        	}
        }

        if(!mediaFileName.getSha1().equals(metadata.getSha1())) {
        	return ValidationResult.HASHCODE_INCONSISTENT;
        }
        
    	return ValidationResult.OK;
    }

    private File getMetadataJsonFile(File file, MediaFileName mediaFileName) {
        return new File(file.getParent(),
                String.format("b%s.json", mediaFileName.getSha1()));
    }

    public static enum ValidationResult {
    	FILE_DOES_NOT_EXIST("The file does not exist (or is a directory)."),
    	FILENAME_NOT_VALID("The filename is not a valid Biography archived media file name."),
    	FILE_IS_NOT_IN_CORRECT_ARCHIVE_FILDER("The file is not located in the correct archive folder."),
    	FILE_HAS_NO_METADATA("The file has no valid Biography metadata."),
    	DATETIME_ORIGINAL_INCONSISTENT("The datetime/original date is inconsistent between filename and Biography metadata."),
    	METADATA_INCONSISTENT("The data inconsistent between EXIF metadata and Biography metadata."),
    	HASHCODE_INCONSISTENT("The hashcode is inconsistent between filename and Biography metadata."),
    	OK("OK");
    	
    	private String message;
		private ValidationResult(String message) {
			this.message = message;
		}
		public String getMessage() {
			return message;
		}
    }
    
}

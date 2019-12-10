package de.zaunkoenigweg.biography.core.archivemetadata;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileName;
import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.MetadataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;

/**
 * This service offers methods to read/write the metadata of archived media files.
 *
 * The methods that edit or read the metadata test consistency/validity of the file before they
 * change anything by calling {@link ArchiveValidationService#isValid(File)}.
 * If the files are not valid, the method throws a {@link IllegalStateException}. 
 * 
 * The changes are performed in a way that should guarantee consistency/validity.
 * 
 * After a change, the editing methods test the files again. They throw an Exception
 * if the validity was damaged (unlikely).
 * 
 */
@Component
public class ArchiveMetadataService {

    private final static Log LOG = LogFactory.getLog(ArchiveMetadataService.class);

    private MetadataService metadataService;
    private ArchiveValidationService archiveValidationService;

    public ArchiveMetadataService(MetadataService metadataService, ArchiveValidationService archiveValidationService) {
		this.metadataService = metadataService;
		this.archiveValidationService = archiveValidationService;
		LOG.info("ArchiveMetadataService started.");
	}

    public BiographyMetadata getMetadata(File file) {
    	throwIfUnvalid(file);
    	Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
    	if(!mediaFileType.isPresent()) {
    		throw new IllegalStateException(String.format("The file type '%s' is not known.", file.getAbsolutePath()));
    	}
		if(ExifDataService.supports(mediaFileType.get())) {
			return metadataService.readMetadataFromExif(file);
    	} else {
    		return metadataService.readMetadataFromJsonFile(getMetadataJsonFile(file));
    	}
    }
    
    public String getDescription(File file) {
    	BiographyMetadata metadata = getMetadata(file);
    	return StringUtils.trimToEmpty(metadata.getDescription());
    }
    
    public void setDescription(File file, String description) {
    	BiographyMetadata metadata = getMetadata(file);
    	metadata = metadata.withDescription(description);
    	Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
    	if(!mediaFileType.isPresent()) {
    		throw new IllegalStateException(String.format("The file type '%s' is not known.", file.getAbsolutePath()));
    	}
		if(ExifDataService.supports(mediaFileType.get())) {
			metadataService.writeMetadataIntoExif(file, metadata);
    	} else {
    		metadataService.writeMetadataToJsonFile(getMetadataJsonFile(file), metadata);
    	}
    }
    
    public void removeAlbums(File file, Set<Album> albums) {
    	BiographyMetadata metadata = getMetadata(file);
    	metadata = metadata.withReducedAlbums(albums);
    	Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
    	if(!mediaFileType.isPresent()) {
    		throw new IllegalStateException(String.format("The file type '%s' is not known.", file.getAbsolutePath()));
    	}
		if(ExifDataService.supports(mediaFileType.get())) {
			metadataService.writeMetadataIntoExif(file, metadata);
    	} else {
    		metadataService.writeMetadataToJsonFile(getMetadataJsonFile(file), metadata);
    	}
    }
    
    public void removeAlbum(File file, Album album) {
    	removeAlbums(file, Collections.singleton(album));
    }
    
    public void addAlbums(File file, Set<Album> albums) {
    	BiographyMetadata metadata = getMetadata(file);
    	metadata = metadata.withMergedAlbums(albums);
    	Optional<MediaFileType> mediaFileType = MediaFileType.of(file);
    	if(!mediaFileType.isPresent()) {
    		throw new IllegalStateException(String.format("The file type '%s' is not known.", file.getAbsolutePath()));
    	}
		if(ExifDataService.supports(mediaFileType.get())) {
			metadataService.writeMetadataIntoExif(file, metadata);
    	} else {
    		metadataService.writeMetadataToJsonFile(getMetadataJsonFile(file), metadata);
    	}
    }
    
    public void addAlbum(File file, Album album) {
    	addAlbums(file, Collections.singleton(album));
    }
    
    private File getMetadataJsonFile(File file) {
    	return new File(file.getParent(), String.format("b%s.json", MediaFileName.of(file.getName()).getSha1()));
    }
    
    private void throwIfUnvalid(File file) {
    	if(!archiveValidationService.isValid(file)) {
    		throw new IllegalStateException(String.format("The file '%s' is not valid.", file.getAbsolutePath()));
    	}
    }
    
    
}

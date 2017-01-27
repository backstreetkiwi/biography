package de.zaunkoenigweg.biography.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

public class MetadataService {

    private final static Log LOG = LogFactory.getLog(MetadataService.class);

    public void setMetadata(File archiveFile, BiographyMetadata metadata) {
        if(archiveFile==null || !archiveFile.exists() || !archiveFile.isFile()) {
            LOG.warn("invalid archive file.");
            return;
        }
        Optional<MediaFileType> mediaFileType = MediaFileType.of(archiveFile);
        if(!mediaFileType.isPresent()) {
            LOG.warn("Unknown Media File Type.");
            return;
        }
        if(ExifData.supports(mediaFileType.get())) {
            setMetadataExif(archiveFile, metadata);
            return;
        }
        setMetadataJsonFile(archiveFile, metadata);
    }
    
    public BiographyMetadata  getMetadata(File archiveFile) {
        if(archiveFile==null || !archiveFile.exists() || !archiveFile.isFile()) {
            LOG.warn("invalid archive file.");
            return null;
        }
        Optional<MediaFileType> mediaFileType = MediaFileType.of(archiveFile);
        if(!mediaFileType.isPresent()) {
            LOG.warn("Unknown Media File Type.");
            return null;
        }
        if(ExifData.supports(mediaFileType.get())) {
            return getMetadataExif(archiveFile);
        }
        return getMetadataJsonFile(archiveFile);
    }
    
    private void setMetadataExif(File archiveFile, BiographyMetadata metadata) {
        ExifData.setUserComment(archiveFile, metadata.toJson());
        // otherwise the description gets destroyed !?
        ExifData.setDescription(archiveFile, metadata.getDescription());
    }

    private BiographyMetadata getMetadataExif(File archiveFile) {
        ExifData exifData = ExifData.of(archiveFile);
        if(exifData==null) {
            return null;
        }
        Optional<String> userComment = exifData.getUserComment();
        if(!userComment.isPresent()) {
            return null;
        }
        return BiographyMetadata.from(userComment.get());
    }

    private void setMetadataJsonFile(File archiveFile, BiographyMetadata metadata) {
        File folder = archiveFile.getParentFile();
        if(folder==null || !folder.isDirectory()) {
            LOG.warn("Parent directory could not be detected.");
            return;
        }
        String jsonFileName = "b" + BiographyFileUtils.sha1(archiveFile) + ".json";
        try {
            FileUtils.write(new File(folder, jsonFileName), metadata.toJson(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Error writing metadata JSON file.", e);
            return;
        }
    }

    private BiographyMetadata getMetadataJsonFile(File archiveFile) {
        File folder = archiveFile.getParentFile();
        if(folder==null || !folder.isDirectory()) {
            LOG.warn("Parent directory could not be detected.");
            return null;
        }
        File jsonFile = new File(folder, "b" + BiographyFileUtils.sha1(archiveFile) + ".json");
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

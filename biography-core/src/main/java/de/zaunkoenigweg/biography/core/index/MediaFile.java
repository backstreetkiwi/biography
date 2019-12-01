package de.zaunkoenigweg.biography.core.index;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Biography media file information from index.
 */
public class MediaFile {
    
    private String fileName;
    private Optional<String> description;
    private List<String> albums;
    
    MediaFile(String fileName, String description, List<String> albums) {
        this.fileName = fileName;
        this.description = Optional.ofNullable(description);
        this.albums = albums;
    }
    public String getFileName() {
        return fileName;
    }
    public String getDescription() {
        return description.orElse("");
    }
    public List<String> getAlbums() {
        if(this.albums!=null) {
            return this.albums;
        }
        return Collections.emptyList();
    }
    
    

}

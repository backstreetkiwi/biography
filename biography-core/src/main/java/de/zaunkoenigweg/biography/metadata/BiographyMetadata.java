package de.zaunkoenigweg.biography.metadata;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Metadata for all Biography Media Files.
 * 
 * This Metadata can be exported to/imported from JSON and stored in separate files or EXIF Metadata.
 * 
 * @author mail@nikolaus-winter.de
 */
public class BiographyMetadata {

    private List<String> albums = new ArrayList<>();

    @SuppressWarnings("unused") // for Gson
    private BiographyMetadata() {
    }
    
    public BiographyMetadata(List<String> albums) {
        this.albums = albums;
    }

    public List<String> getAlbums() {
        return albums;
    }
    
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    public static BiographyMetadata from(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, BiographyMetadata.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}

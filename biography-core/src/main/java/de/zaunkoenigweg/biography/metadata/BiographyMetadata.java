package de.zaunkoenigweg.biography.metadata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

/**
 * Metadata for all Biography Media Files.
 * 
 * This Metadata can be exported to/imported from JSON and stored in separate files or EXIF Metadata.
 * 
 * @author mail@nikolaus-winter.de
 */
public class BiographyMetadata {

    private LocalDateTime dateTimeOriginal;
    private String description;
    private List<Album> albums = new ArrayList<>();

    @SuppressWarnings("unused") // for Gson
    private BiographyMetadata() {
    }
    
    public BiographyMetadata(LocalDateTime dateTimeOriginal, String description, List<Album> albums) {
        this.dateTimeOriginal = dateTimeOriginal;
        this.description = description;
        this.albums = albums;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, localDateTimeSerializer).create();
        return gson.toJson(this);
    }
    
    public static BiographyMetadata from(String json) {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, localDateTimeDeserializer).create();
        try {
            return gson.fromJson(json, BiographyMetadata.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public LocalDateTime getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public String getDescription() {
        return description;
    }

    public List<Album> getAlbums() {
        return albums;
    }
    
    private static final JsonDeserializer<LocalDateTime> localDateTimeDeserializer = (json, typeOfT, context) -> {
        return LocalDateTime.parse(json.getAsString());
    };

    private static final JsonSerializer<LocalDateTime> localDateTimeSerializer = (localDateTime, type, context) -> {
        return new JsonPrimitive(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
    };

}

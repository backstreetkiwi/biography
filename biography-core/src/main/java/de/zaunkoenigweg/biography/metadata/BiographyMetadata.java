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
    
    private static final JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_SERIALIZER = (localDateTime, type, context) -> {
    	return new JsonPrimitive(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime));
    };

    private static final Gson SERIALIZER = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER).create(); 
    
    private static final JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_DESERIALIZER = (json, typeOfT, context) -> {
        return LocalDateTime.parse(json.getAsString());
    };
    
    private static final Gson DESERIALIZER = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER).create();

    /**
     * This constructor is just used to create a Metadata object through Gson.
     */
    @SuppressWarnings("unused")
    private BiographyMetadata() {
    }
    
    public BiographyMetadata(LocalDateTime dateTimeOriginal, String description, List<Album> albums) {
        this.dateTimeOriginal = dateTimeOriginal;
        this.description = description;
        this.albums = albums;
    }

    /**
     * Exports this biography metadata to JSON.
     * @return biography metadata as JSON string.
     */
    public String toJson() {
        return SERIALIZER.toJson(this);
    }
    
    /**
     * Creates a Biography metadata object from JSON string.
     * @param json JSON String
     * @return BiographyMetadata
     */
    public static BiographyMetadata from(String json) {
        try {
            return DESERIALIZER.fromJson(json, BiographyMetadata.class);
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
    
}

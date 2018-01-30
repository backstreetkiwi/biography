package de.zaunkoenigweg.biography.metadata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private Set<Album> albums = new HashSet<>();
    
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
    
    public BiographyMetadata(LocalDateTime dateTimeOriginal, String description, Set<Album> albums) {
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
    
    public Set<Album> getAlbums() {
        return albums;
    }
    
    /**
     * Produces a metadata object that contains the same data as this one, with the description
     * replaced by the given value.
     * 
     * @param newDescription new description
     * @return metadata object with new description
     */
    public BiographyMetadata withDescription(String newDescription) {
    	return new BiographyMetadata(this.dateTimeOriginal, newDescription, Collections.unmodifiableSet(albums));
    }

    /**
     * Produces a metadata object that contains the same data as this one, with the albums
     * replaced by the given album list.
     * 
     * @param newAlbums new list of albums
     * @return metadata object with new description
     */
    public BiographyMetadata withAlbums(Set<Album> newAlbums) {
    	return new BiographyMetadata(this.dateTimeOriginal, this.description, Collections.unmodifiableSet(newAlbums));
    }


    /**
     * Produces a metadata object that contains the same data as this one, with the albums
     * merged with the albums of the given album list.
     * 
     * @param newAlbums list of albums to merge into the existing album list
     * @return metadata object with new description
     */
    public BiographyMetadata withMergedAlbums(Set<Album> newAlbums) {
    	HashSet<Album> mergedAlbums = new HashSet<>(this.albums);
    	mergedAlbums.addAll(newAlbums);
    	return new BiographyMetadata(this.dateTimeOriginal, this.description, Collections.unmodifiableSet(mergedAlbums));
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((albums == null) ? 0 : albums.hashCode());
		result = prime * result + ((dateTimeOriginal == null) ? 0 : dateTimeOriginal.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
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
		BiographyMetadata other = (BiographyMetadata) obj;
		if (albums == null) {
			if (other.albums != null)
				return false;
		} else if (!albums.equals(other.albums))
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
		return true;
	}
    
    

}

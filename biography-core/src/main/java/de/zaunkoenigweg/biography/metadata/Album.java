package de.zaunkoenigweg.biography.metadata;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Album (as part of Biography metadata).
 * 
 * An album MUST have a title and MAY have a chapter.
 * 
 * @author mail@nikolaus-winter.de
 */
public class Album {

    public static final String SEPARATOR = "|";
    
    private String title;
    private String chapter;
    
    /**
     * This constructor is just used to create a Metadata object through Gson.
     */
    @SuppressWarnings("unused")
    private Album() {
    }
    
    /**
     * Creates Album metadata with just a title (no chapter).
     * @param title Title, must not be empty.
     */
    public Album(String title) {
        if(StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Album title must not be blank.");
        }
        if(StringUtils.contains(title, SEPARATOR)) {
            throw new IllegalArgumentException(String.format("Album title must not contain the separator String (\"%s\").", SEPARATOR));
        }
        this.title = title;
    }

    /**
     * Creates Album metadata with a title and a chapter.
     * @param title Title, must not be empty.
     * @param chapter Chapter, must not be empty.
     */
    public Album(String title, String chapter) {
        if(StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Album title must not be blank.");
        }
        if(StringUtils.isBlank(chapter)) {
            throw new IllegalArgumentException("Album chapter must not be blank.");
        }
        if(StringUtils.contains(chapter, SEPARATOR)) {
            throw new IllegalArgumentException(String.format("Album chapter must not contain the separator String (\"%s\").", SEPARATOR));
        }
        if(StringUtils.contains(title, SEPARATOR)) {
            throw new IllegalArgumentException(String.format("Album title must not contain the separator String (\"%s\").", SEPARATOR));
        }
        this.title = title;
        this.chapter = chapter;
    }

    /**
     * Exports this album metadata to JSON.
     * @return album metadata as JSON string.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    /**
     * Creates an album metadata object from JSON string.
     * @param json JSON String
     * @return Album
     */
    public static Album fromJson(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, Album.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("No valid Json String: " + json);
        }
    }

    /**
     * Creates an album metadata object from an id string.
	 * 
	 * An album ID has the format "title|chapter" or "title|"
	 *  
     * @param id album ID string
     * @return Album
     */
    public static Album fromId(String id) {
        if(StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("Album id must not be blank.");
        }
        if(!StringUtils.contains(id, SEPARATOR)) {
            throw new IllegalArgumentException("Album id must contain a separator.");
        }
        String[] tokens = StringUtils.splitByWholeSeparator(id, SEPARATOR);
        if(tokens.length!=2) {
            throw new IllegalArgumentException("Album id is not valid.");
        }
        if(StringUtils.isBlank(tokens[1])) {
            return new Album(tokens[0]);
        }
        return new Album(tokens[0], tokens[1]);
    }
    
    /**
     * Gets the album ID string
     * 
     * See {@link #fromId(String)} for information about the format.
     * 
     * @return album ID string
     */
    public String getId() {
        return String.format("%s%s%s", this.title, SEPARATOR, this.chapter != null ? this.chapter : "");
    }

    public String getTitle() {
        return title;
    }
    
    public Optional<String> getChapter() {
        return Optional.ofNullable(this.chapter);
    }

    @Override
    public String toString() {
    	return toJson();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chapter == null) ? 0 : chapter.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
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
        Album other = (Album) obj;
        if (chapter == null) {
            if (other.chapter != null)
                return false;
        } else if (!chapter.equals(other.chapter))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }
}

package de.zaunkoenigweg.biography.metadata;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Album (as part of Biography metadata).
 * 
 * ValueObject
 */
public class Album {

    private String title;
    
    /**
     * This constructor is just used to create a metadata object through Gson.
     */
    @SuppressWarnings("unused")
    private Album() {
    }
    
    /**
     * Creates Album metadata.
     * @param title Title, must not be empty.
     */
    public Album(String title) {
        if(StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Album title must not be blank.");
        }
        this.title = title;
    }

    /**
     * Exports this album metadata to JSON.
     * @return album metadata as JSON string.
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
    
    /**
     * Creates an album metadata object from JSON string.
     * @param json JSON String
     * @return Album
     */
    public static Album fromJson(String json) {
        try {
            return new Gson().fromJson(json, Album.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("No valid Json String: " + json);
        }
    }

    public String getTitle() {
        return title;
    }
    
    @Override
    public String toString() {
    	return toJson();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
    
    
}

package de.zaunkoenigweg.biography.metadata;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Album.
 * 
 * An album must have a title and may have a chapter.
 * 
 * @author mail@nikolaus-winter.de
 */
public class Album {

    private String title;
    private String chapter;
    
    @SuppressWarnings("unused") // for Gson
    private Album() {
    }
    
    public Album(String title) {
        this.title = title;
    }

    public Album(String title, String chapter) {
        this.title = title;
        this.chapter = chapter;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    public static Album from(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, Album.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getChapter() {
        return Optional.ofNullable(this.chapter);
    }
}

package de.zaunkoenigweg.biography.metadata;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

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

    public static final String SEPARATOR = "::";
    
    private String id;
    private String title;
    private String chapter;
    
    @SuppressWarnings("unused") // for Gson
    private Album() {
    }
    
    public Album(String title) {
        if(StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Album title must not be blank.");
        }
        if(StringUtils.contains(title, SEPARATOR)) {
            throw new IllegalArgumentException(String.format("Album title must not contain the separator String (\"%s\").", SEPARATOR));
        }
        this.id = title;
        this.title = title;
    }

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
        this.id = String.format("%s%s%s", title, SEPARATOR, chapter);
        this.title = title;
        this.chapter = chapter;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(id);
    }
    
    public static Album fromJson(String json) {
        Gson gson = new Gson();
        try {
            String id = gson.fromJson(json, String.class);
            return fromId(id);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("No valid Json String: " + json);
        }
    }

    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }

    public Optional<String> getChapter() {
        return Optional.ofNullable(this.chapter);
    }
    
    private static Album fromId(String id) {
        if(StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("Album id must not be blank.");
        }
        if(!StringUtils.contains(id, SEPARATOR)) {
            return new Album(id);
        }
        String[] tokens = StringUtils.splitByWholeSeparator(id, SEPARATOR);
        if(tokens.length!=2) {
            throw new IllegalArgumentException("Album id is not valid.");
        }
        return new Album(tokens[0], tokens[1]);
    }
}

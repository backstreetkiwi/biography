package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class AlbumTest {

    @Before
    public void setUp() throws IOException {
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateTitleNull() {
        new Album(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateEmptyTitle() {
        new Album("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateBlankTitle() {
        new Album("  ");
    }

    @Test
    public void testToJsonTitleOnly() {
        Album album = new Album("NZ 2005");
        String json = album.toJson();
        assertEquals("{\"title\":\"NZ 2005\"}", json);
    }

    @Test
    public void testFromJsonTitleOnly() {
        String json = "{\"title\":\"NZ 2005\"}";
        Album album = Album.fromJson(json);
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertEquals("NZ 2005", album.getTitle());
    }
}

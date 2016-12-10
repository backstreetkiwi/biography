package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class AlbumTest {

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void testToJsonTitleOnly() {
        Album album = new Album("NZ 2005");
        String json = album.toJson();
        assertEquals("{\"title\":\"NZ 2005\"}", json);
    }

    @Test
    public void testToJsonTitleAndChapter() {
        Album album = new Album("NZ 2005", "03 Kaiteriteri");
        String json = album.toJson();
        assertEquals("{\"title\":\"NZ 2005\",\"chapter\":\"03 Kaiteriteri\"}", json);
    }

    @Test
    public void testFromJsonTitleOnly() {
        String json = "{\"title\":\"NZ 2005\"}";
        Album album = Album.from(json);
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertFalse(album.getChapter().isPresent());
        assertEquals("NZ 2005", album.getTitle());
    }

    @Test
    public void testFromJsonTitleAndChapter() {
        String json = "{\"title\":\"NZ 2005\",\"chapter\":\"03 Kaiteriteri\"}";
        Album album = Album.from(json);
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertTrue(album.getChapter().isPresent());
        assertEquals("NZ 2005", album.getTitle());
        assertEquals("03 Kaiteriteri", album.getChapter().get());
    }

    @Test
    public void testFromJsonEmptyJson() {
        String json = "{}";
        Album album = Album.from(json);
        assertNotNull(album);
        assertNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertFalse(album.getChapter().isPresent());
        assertNotNull(album);
    }

    @Test
    public void testFromJsonEmptyString() {
        String json = "";
        Album album = Album.from(json);
        assertNull(album);
    }

    @Test
    public void testFromJsonBogusString() {
        String json = "bogus";
        Album album = Album.from(json);
        assertNull(album);
    }

}

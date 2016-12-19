package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test(expected=IllegalArgumentException.class)
    public void testCreateTitleContainsSeparator() {
        new Album("title" + Album.SEPARATOR + "something");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateChapterNull() {
        new Album("title", null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateEmptyChapter() {
        new Album("title", "");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateBlankChapter() {
        new Album("title", "  ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateChapterContainsSeparator() {
        new Album("title", "something" + Album.SEPARATOR + "chapter");
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
        Album album = Album.fromJson(json);
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertFalse(album.getChapter().isPresent());
        assertEquals("NZ 2005", album.getTitle());
    }

    @Test
    public void testFromJsonTitleAndChapter() {
        String json = "{\"title\":\"NZ 2005\",\"chapter\":\"03 Kaiteriteri\"}";
        Album album = Album.fromJson(json);
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertTrue(album.getChapter().isPresent());
        assertEquals("NZ 2005", album.getTitle());
        assertEquals("03 Kaiteriteri", album.getChapter().get());
    }

    @Test
    public void testToIdTitleOnly() {
        Album album = new Album("NZ 2005");
        String id = album.getId();
        assertEquals("NZ 2005" + Album.SEPARATOR, id);
    }

    @Test
    public void testFromIdTitleOnly() {
        Album album = Album.fromId("NZ 2005" + Album.SEPARATOR);
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertFalse(album.getChapter().isPresent());
        assertEquals("NZ 2005", album.getTitle());
    }

    @Test
    public void testToIdTitleAndChapter() {
        Album album = new Album("NZ 2005", "03 Kaiteriteri");
        String id = album.getId();
        assertEquals("NZ 2005" + Album.SEPARATOR + "03 Kaiteriteri", id);
    }

    @Test
    public void testFromIdTitleAndChapter() {
        Album album = Album.fromId("NZ 2005" + Album.SEPARATOR + "03 Kaiteriteri");
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertNotNull(album.getChapter());
        assertTrue(album.getChapter().isPresent());
        assertEquals("NZ 2005", album.getTitle());
        assertEquals("03 Kaiteriteri", album.getChapter().get());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIdMultipleSeparators() {
        Album.fromId("NZ 2005" + Album.SEPARATOR + "something" + Album.SEPARATOR + "03 Kaiteriteri");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIdNoTitleBeforeSeparator() {
        Album.fromId(Album.SEPARATOR + "03 Kaiteriteri");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIdNoSeparator() {
        Album.fromId("Title without any separator");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIdEmptyString() {
        Album.fromId("");
    }

}

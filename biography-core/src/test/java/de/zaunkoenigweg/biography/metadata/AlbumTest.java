package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AlbumTest {

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
    public void testToJson() {
        assertEquals("{\"title\":\"NZ 2005\"}", new Album("NZ 2005").toJson());
    }

    @Test
    public void testFromJson() {
        Album album = Album.fromJson("{\"title\":\"NZ 2005\"}");
        assertNotNull(album);
        assertNotNull(album.getTitle());
        assertEquals("NZ 2005", album.getTitle());
    }
}

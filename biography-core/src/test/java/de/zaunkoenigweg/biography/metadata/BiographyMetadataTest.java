package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BiographyMetadataTest {

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void testToJson() {
        List<Album> albums = Arrays.asList(new Album("NZ 2005", "03 Kaiteriteri"), new Album("NZ 2007"));
        BiographyMetadata biographyMetadata = new BiographyMetadata(albums);
        String json = biographyMetadata.toJson();
        assertEquals("{\"albums\":[{\"title\":\"NZ 2005\",\"chapter\":\"03 Kaiteriteri\"},{\"title\":\"NZ 2007\"}]}", json);
    }

    @Test
    public void testFromJson() {
        String json = "{\"albums\":[{\"title\":\"NZ 2005\",\"chapter\":\"03 Kaiteriteri\"},{\"title\":\"NZ 2007\"}]}";
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNotNull(biographyMetadata);
        assertNotNull(biographyMetadata.getAlbums());
        assertEquals(2, biographyMetadata.getAlbums().size());
        assertNotNull(biographyMetadata.getAlbums().get(0));
        assertNotNull(biographyMetadata.getAlbums().get(0).getTitle());
        assertNotNull(biographyMetadata.getAlbums().get(0).getChapter());
        assertTrue(biographyMetadata.getAlbums().get(0).getChapter().isPresent());
        assertEquals("NZ 2005", biographyMetadata.getAlbums().get(0).getTitle());
        assertEquals("03 Kaiteriteri", biographyMetadata.getAlbums().get(0).getChapter().get());
        assertNotNull(biographyMetadata.getAlbums().get(1));
        assertNotNull(biographyMetadata.getAlbums().get(1).getTitle());
        assertNotNull(biographyMetadata.getAlbums().get(1).getChapter());
        assertFalse(biographyMetadata.getAlbums().get(1).getChapter().isPresent());
        assertEquals("NZ 2007", biographyMetadata.getAlbums().get(1).getTitle());
    }

    @Test
    public void testFromJsonEmptyJson() {
        String json = "{}";
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNotNull(biographyMetadata);
        assertNotNull(biographyMetadata.getAlbums());
        assertEquals(0, biographyMetadata.getAlbums().size());
    }

    @Test
    public void testFromJsonEmptyString() {
        String json = "";
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNull(biographyMetadata);
    }

    @Test
    public void testFromJsonBogusString() {
        String json = "bogus";
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNull(biographyMetadata);
    }

}

package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        List<String> albums = Arrays.asList("Holiday 2007", "Child XY is born (2013)");
        BiographyMetadata biographyMetadata = new BiographyMetadata(albums);
        String json = biographyMetadata.toJson();
        assertEquals("{\"albums\":[\"Holiday 2007\",\"Child XY is born (2013)\"]}", json);
    }

    @Test
    public void testFromJson() {
        String json = "{\"albums\":[\"Holiday 2007\",\"Child XY is born (2013)\"]}";
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNotNull(biographyMetadata);
        assertNotNull(biographyMetadata.getAlbums());
        assertEquals(2, biographyMetadata.getAlbums().size());
        assertEquals("Holiday 2007", biographyMetadata.getAlbums().get(0));
        assertEquals("Child XY is born (2013)", biographyMetadata.getAlbums().get(1));
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

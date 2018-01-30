package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class BiographyMetadataTest {

	private static final Album ALBUM_1 = new Album("album_1_title_aksdfj");
    private static final Album ALBUM_2 = new Album("album_2_title_gdtfbf");
    private static final LocalDateTime DATE_TIME_ORIGINAL = LocalDateTime.of(2005, 02, 05, 15, 27, 33, 123000000);
	private static final Set<Album> ALBUMS = new HashSet<>(Arrays.asList(ALBUM_1, ALBUM_2));
	private static final String DESCRIPTION = "description";
	private static final BiographyMetadata BIOGRAPHY_METADATA = new BiographyMetadata(DATE_TIME_ORIGINAL, DESCRIPTION, ALBUMS);
	private String json;

	@Before
    public void setUp() throws IOException {
		json = String.format(
				"{\"dateTimeOriginal\":\"2005-02-05T15:27:33.123\","
				+ "\"description\":\"%s\","
				+ "\"albums\":[{\"title\":\"%s\"},"
				+ "{\"title\":\"%s\"}]}", 
				DESCRIPTION, 
				ALBUM_1.getTitle(), 
				ALBUM_2.getTitle());
		
		
	}

	/**
	 * This test can only test necessary conditions, but is not sufficient
	 * as the JSON cannot be compared as a whole due to unspecified sequence of values.
	 * 
	 * A sufficient test would include parsing the JSON. Maybe later...
	 */
	@Test
    public void testToJson() {
    	String json = BIOGRAPHY_METADATA.toJson();
    	assertNotNull(json);
    	assertTrue(StringUtils.isNotBlank(json));
    	assertTrue(StringUtils.contains(json, ALBUM_1.getTitle()));
    	assertTrue(StringUtils.contains(json, ALBUM_2.getTitle()));
    }

    @Test
    public void testFromJson() {
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNotNull(biographyMetadata);
        assertEquals(BIOGRAPHY_METADATA, biographyMetadata);
    }

    @Test
    public void testFromJsonEmptyJson() {
        String json = "{}";
        BiographyMetadata biographyMetadata = BiographyMetadata.from(json);
        assertNotNull(biographyMetadata);
        assertNull(biographyMetadata.getDateTimeOriginal());
        assertNull(biographyMetadata.getDescription());
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

    @Test
    public void testWithDescription() {
    	String newDescription = "new_description";
		BiographyMetadata biographyMetadata = BIOGRAPHY_METADATA.withDescription(newDescription);
		
		assertNotNull(biographyMetadata);
		assertFalse(biographyMetadata==BIOGRAPHY_METADATA);
		assertEquals(BIOGRAPHY_METADATA.getDateTimeOriginal(), biographyMetadata.getDateTimeOriginal());
		assertEquals(BIOGRAPHY_METADATA.getAlbums(), biographyMetadata.getAlbums());
		assertEquals(newDescription, biographyMetadata.getDescription());
    }

    @Test
    public void testWithAlbums() {
    	Set<Album> newAlbums = new HashSet<>();
    	newAlbums.add(new Album("new_album_1_title_azdteg"));
    	newAlbums.add(new Album("new_album_2_title_ssrcwg"));
		BiographyMetadata biographyMetadata = BIOGRAPHY_METADATA.withAlbums(newAlbums);
		
		assertNotNull(biographyMetadata);
		assertFalse(biographyMetadata==BIOGRAPHY_METADATA);
		assertEquals(BIOGRAPHY_METADATA.getDateTimeOriginal(), biographyMetadata.getDateTimeOriginal());
		assertEquals(BIOGRAPHY_METADATA.getDescription(), biographyMetadata.getDescription());
		assertEquals(newAlbums, biographyMetadata.getAlbums());
    }

    @Test
    public void testWithMergedAlbums() {
    	Set<Album> newAlbums = new HashSet<>();
    	newAlbums.add(new Album("new_album_1_title_azdteg"));
    	newAlbums.add(new Album("new_album_2_title_ssrcwg"));
    	newAlbums.add(ALBUM_1);
		BiographyMetadata biographyMetadata = BIOGRAPHY_METADATA.withMergedAlbums(newAlbums);
		
		assertNotNull(biographyMetadata);
		assertFalse(biographyMetadata==BIOGRAPHY_METADATA);
		assertEquals(BIOGRAPHY_METADATA.getDateTimeOriginal(), biographyMetadata.getDateTimeOriginal());
		assertEquals(BIOGRAPHY_METADATA.getDescription(), biographyMetadata.getDescription());
		
		Set<Album> mergedAlbums = biographyMetadata.getAlbums();
		assertTrue(mergedAlbums.containsAll(newAlbums));
		assertTrue(mergedAlbums.containsAll(ALBUMS));
		assertEquals(ALBUMS.size() + newAlbums.size() - 1, mergedAlbums.size());
		
    }
    
    @Test
    public void testWithMergedAlbumsDisjoint() {
    	Set<Album> newAlbums = new HashSet<>();
    	newAlbums.add(new Album("new_album_1_title_azdteg"));
    	newAlbums.add(new Album("new_album_2_title_ssrcwg"));
		BiographyMetadata biographyMetadata = BIOGRAPHY_METADATA.withMergedAlbums(newAlbums);
		
		assertNotNull(biographyMetadata);
		assertFalse(biographyMetadata==BIOGRAPHY_METADATA);
		assertEquals(BIOGRAPHY_METADATA.getDateTimeOriginal(), biographyMetadata.getDateTimeOriginal());
		assertEquals(BIOGRAPHY_METADATA.getDescription(), biographyMetadata.getDescription());
		
		Set<Album> mergedAlbums = biographyMetadata.getAlbums();
		assertTrue(mergedAlbums.containsAll(newAlbums));
		assertTrue(mergedAlbums.containsAll(ALBUMS));
		assertEquals(ALBUMS.size() + newAlbums.size(), mergedAlbums.size());
		
    }
}

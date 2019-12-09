package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.time.LocalDateTime;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MediaFileNameTest {

	@Test
	public void testIsValid() {
		assertFalse(MediaFileName.isValid(null));
		assertFalse(MediaFileName.isValid(""));
		assertFalse(MediaFileName.isValid(" "));
		assertFalse(MediaFileName.isValid("hurz"));
		assertFalse(MediaFileName.isValid("2019-0-0--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg"));
		assertFalse(MediaFileName.isValid("2019-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba45.jpg"));
		assertFalse(MediaFileName.isValid("2019-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26bag.jpg"));
		assertFalse(MediaFileName.isValid("2019-02-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg"));
		assertFalse(MediaFileName.isValid("2019-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.abc"));
		assertFalse(MediaFileName.isValid("2019-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.mpeg"));
		assertFalse(MediaFileName.isValid("2019-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.tif"));
		assertFalse(MediaFileName.isValid("hallo2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpgende"));
		assertTrue(MediaFileName.isValid("2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg"));
		assertTrue(MediaFileName.isValid("2016-02-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg"));
		assertTrue(MediaFileName.isValid("2016-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.mov"));
		assertTrue(MediaFileName.isValid("2016-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.avi"));
		assertTrue(MediaFileName.isValid("2016-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.mp4"));
		assertTrue(MediaFileName.isValid("2016-03-29--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.mpg"));
	}

	@Test
	public void testFactoryMethodOf() {
		String filename = "2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg";
		MediaFileName mediaFileName = MediaFileName.of(filename);
		assertNotNull(mediaFileName);
		assertEquals(filename, mediaFileName.getFilename());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFactoryMethodOfFailure() {
		MediaFileName.of("hurz");
	}

	@Test
	public void testGetType() {
		String filename = "2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg";
		MediaFileName mediaFileName = MediaFileName.of(filename);
		assertEquals(MediaFileType.JPEG, mediaFileName.getType());
	}

	@Test
	public void testGetDateTimeOriginal() {
		String filename = "2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg";
		MediaFileName mediaFileName = MediaFileName.of(filename);
		assertEquals(LocalDateTime.of(2019, 2, 28, 17, 31, 33), mediaFileName.getDateTimeOriginal());
	}

	@Test
	public void testGetSha1() {
		String filename = "2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg";
		MediaFileName mediaFileName = MediaFileName.of(filename);
		assertEquals("c69239ffcc01886f9d73ecd1076271bb65c26ba4", mediaFileName.getSha1());
	}

	@Test(expected=NullPointerException.class)
	public void testArchiveFileNull() {
		MediaFileName mediaFileName = MediaFileName.of("2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg");
		mediaFileName.archiveFile(null);
	}

	@Test
	public void testArchiveFile() {
		MediaFileName mediaFileName = MediaFileName.of("2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg");
		mediaFileName.archiveFile(new File("/home/biography/archive"));
		assertEquals(new File("/home/biography/archive/2019/02/2019-02-28--17-31-33---c69239ffcc01886f9d73ecd1076271bb65c26ba4.jpg"), mediaFileName.archiveFile(new File("/home/biography/archive")));
	}
}

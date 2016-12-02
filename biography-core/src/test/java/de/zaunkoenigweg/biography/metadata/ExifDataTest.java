package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class ExifDataTest {

    private File someFolder;
    private File notExistingFile;
    private File someEmptyFile;

    @Before
    public void setUp() throws IOException {
        someFolder = Files.createTempDirectory("someFolder").toFile();
        someFolder.deleteOnExit();
        notExistingFile = new File(someFolder, "i-do-not-exist");
        someEmptyFile = Files.createTempFile("someFile", null).toFile();
    }

    @Test
    public void testReadNullFile() {
    	ExifData exifData = ExifData.of(null);
    	assertNull(exifData);
    }
    
    @Test
    public void testReadFolder() {
    	ExifData exifData = ExifData.of(someFolder);
    	assertNull(exifData);
    }
    
    @Test
    public void testReadNotExistingFile() {
    	ExifData exifData = ExifData.of(notExistingFile);
    	assertNull(exifData);
    }
    
    @Test
    public void testReadEmptyFile() {
    	ExifData exifData = ExifData.of(someEmptyFile);
    	assertNull(exifData);
    }
    
    @Test
    public void testReadIPhone5sPhoto() {
        File file = new File(getClass().getResource("/exifdatatest/iPhone5s.jpg").getFile());
        ExifData exifData = ExifData.of(file);
        assertNotNull(exifData);
        LocalDateTime expectedDateTimeOriginal = LocalDateTime.of(2016, 7, 20, 21, 4, 13, toNanos(660));
        assertEquals(expectedDateTimeOriginal, exifData.getDateTimeOriginal());
    }
    
    @Test
    public void testReadJpegWithoutDatetimeOriginal() {
        File file = new File(getClass().getResource("/exifdatatest/NoDatetimeOriginal.jpg").getFile());
        ExifData exifData = ExifData.of(file);
        assertNotNull(exifData);
        assertNull(exifData.getDateTimeOriginal());
    }
    
    @Test
    public void testReadCasioQV5700Photo() {
    	File file = new File(getClass().getResource("/exifdatatest/CasioQV5700.jpg").getFile());
    	ExifData exifData = ExifData.of(file);
    	assertNotNull(exifData);
    	LocalDateTime expectedDateTimeOriginal = LocalDateTime.of(2003, 4, 8, 19, 10, 57);
    	assertEquals(expectedDateTimeOriginal, exifData.getDateTimeOriginal());
    }
    
    @Test
    public void testReadNikonD60Photo() {
    	File file = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    	ExifData exifData = ExifData.of(file);
    	assertNotNull(exifData);
    	LocalDateTime expectedDateTimeOriginal = LocalDateTime.of(2005, 2, 22, 13, 51, 32, toNanos(80));
    	assertEquals(expectedDateTimeOriginal, exifData.getDateTimeOriginal());
    }
    
    @Test
    public void testReadJpegWithoutDescription() {
        File file = new File(getClass().getResource("/exifdatatest/iPhone5s.jpg").getFile());
        ExifData exifData = ExifData.of(file);
        assertNotNull(exifData);
        assertNotNull(exifData.getDescription());
        assertFalse(exifData.getDescription().isPresent());
    }
    
    @Test
    public void testReadJpegDescription() {
        File file = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        ExifData exifData = ExifData.of(file);
        assertNotNull(exifData);
        assertNotNull(exifData.getDescription());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Christchurch; auf dem Cathedral Square", exifData.getDescription().get());
    }

    @Test
    public void testReadJpegDescriptionWithUmlaut() {
        File file = new File(getClass().getResource("/exifdatatest/DescriptionWithUmlaut.jpg").getFile());
        ExifData exifData = ExifData.of(file);
        assertNotNull(exifData);
        assertNotNull(exifData.getDescription());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Landeanflug auf NZ, Nordteil der S\u00fcdinsel", exifData.getDescription().get());
    }
    
    private int toNanos(int millis) {
    	return millis * 1000000;
    }
    
}

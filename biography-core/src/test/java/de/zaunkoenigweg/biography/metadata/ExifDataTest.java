package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

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
    public void testFromWithNullFile() {
    	ExifData exifData = ExifData.from(null);
    	assertNull(exifData);
    }
    
    @Test
    public void testFromWithFolder() {
    	ExifData exifData = ExifData.from(someFolder);
    	assertNull(exifData);
    }
    
    @Test
    public void testFromWithNotExistingFile() {
    	ExifData exifData = ExifData.from(notExistingFile);
    	assertNull(exifData);
    }
    
    @Test
    public void testFromWithEmptyFile() {
    	ExifData exifData = ExifData.from(someEmptyFile);
    	assertNull(exifData);
    }
    
    @Test
    public void testFromWithIPhone5sPhoto() {
    	File file = new File(getClass().getResource("/exifdatatest/iPhone5s.jpg").getFile());
    	ExifData exifData = ExifData.from(file);
    	assertNotNull(exifData);
    	LocalDateTime expectedDateTimeOriginal = LocalDateTime.of(2016, 7, 20, 21, 4, 13, toNanos(660));
    	assertEquals(expectedDateTimeOriginal, exifData.getDateTimeOriginal());
    }
    
    @Test
    public void testFromWithCasioQV5700Photo() {
    	File file = new File(getClass().getResource("/exifdatatest/CasioQV5700.jpg").getFile());
    	ExifData exifData = ExifData.from(file);
    	assertNotNull(exifData);
    	LocalDateTime expectedDateTimeOriginal = LocalDateTime.of(2003, 4, 8, 19, 10, 57);
    	assertEquals(expectedDateTimeOriginal, exifData.getDateTimeOriginal());
    }
    
    @Test
    public void testFromWithNikonD60Photo() {
    	File file = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    	ExifData exifData = ExifData.from(file);
    	assertNotNull(exifData);
    	LocalDateTime expectedDateTimeOriginal = LocalDateTime.of(2005, 2, 22, 13, 51, 15);
    	assertEquals(expectedDateTimeOriginal, exifData.getDateTimeOriginal());
    }
    
    private int toNanos(int millis) {
    	return millis * 1000000;
    }
    
}

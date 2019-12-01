package de.zaunkoenigweg.biography.metadata.exif;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class ExifDataServiceTest {

    private File tempFolder;
    private File someEmptyFile;
    
    private ExifDataService exifDataService;

    @Before
    public void setUp() throws Exception {
        tempFolder = Files.createTempDirectory("tempFolder").toFile();
        tempFolder.deleteOnExit();
        someEmptyFile = Files.createTempFile("someFile", null).toFile();
        exifDataService = new ExifDataService(this.tempFolder);
    }

    @Test
    public void testGetExifDataNull() throws IOException {
        ExifData exifData = exifDataService.readExifData(null);
        assertNull(exifData);
    }

    @Test
    public void testGetExifDataEmptyFile() throws IOException {
        ExifData exifData = exifDataService.readExifData(someEmptyFile);
        assertNull(exifData);
    }

    @Test
    public void testGetExifData() throws IOException {
        File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File file = new File(tempFolder, "NikonD60.jpg");
        Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ExifData exifData = exifDataService.readExifData(file);
        assertNotNull(exifData);
        assertNotNull(exifData.getDateTimeOriginal());
        assertEquals(LocalDateTime.of(2005, 2, 22, 13, 51, 32, 80_000_000), exifData.getDateTimeOriginal());
        assertNotNull(exifData.getDateTimeOriginal());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Christchurch; auf dem Cathedral Square", exifData.getDescription().get());
        assertNotNull(exifData.getUserComment());
        assertFalse(exifData.getUserComment().isPresent());
    }

    @Test
    public void testWriteExifData() throws IOException {
        File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File file = new File(tempFolder, "NikonD60.jpg");
        Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ExifData exifData = exifDataService.readExifData(file);
        assertNotNull(exifData);
        exifData.setDescription("some new description");
        exifDataService.writeExifData(file, exifData);
        ExifData newExifData = exifDataService.readExifData(file);
        assertEquals(newExifData, exifData);
    }
    
}

package de.zaunkoenigweg.biography.metadata.exif;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import de.zaunkoenigweg.biography.core.test.TestUtil;

public class ExifDataServiceTest {

    private File tempFolder;
    private File someEmptyFile;
    
    private ExifDataService exifDataService;

    @Before
    public void setUp() throws Exception {
        tempFolder = Files.createTempDirectory("tempFolder").toFile();
        tempFolder.deleteOnExit();
        someEmptyFile = Files.createTempFile("someFile", null).toFile();
        exifDataService = new ExifDataService();
    }

    @Test
    public void testGetExifDataNull() throws IOException {
        ExifData exifData = exifDataService.getExifData(null);
        assertNull(exifData);
    }

    @Test
    public void testGetExifDataEmptyFile() throws IOException {
        ExifData exifData = exifDataService.getExifData(someEmptyFile);
        assertNull(exifData);
    }

    @Test
    public void testGetExifData() throws IOException {
        File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File file = new File(tempFolder, "NikonD60.jpg");
        Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ExifData exifData = exifDataService.getExifData(file);
        assertNotNull(exifData);
        assertNotNull(exifData.getDateTimeOriginal());
        assertEquals(LocalDateTime.of(2005, 2, 22, 13, 51, 32, 80_000_000), exifData.getDateTimeOriginal());
        assertNotNull(exifData.getDateTimeOriginal());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Christchurch; auf dem Cathedral Square", exifData.getDescription().get());
        assertNotNull(exifData.getCameraModel());
        assertTrue(exifData.getCameraModel().isPresent());
        assertEquals("NIKON D70", exifData.getCameraModel().get());
        assertNotNull(exifData.getUserComment());
        assertFalse(exifData.getUserComment().isPresent());
    }

    
    @Test
    public void testGetExifDataFromCache() throws IOException {
        File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File file = new File(tempFolder, "NikonD60.jpg");
        Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ExifData exifData = exifDataService.getExifData(file);
        assertNotNull(exifData);
        Files.delete(file.toPath());
        ExifData exifDataAfterRemoval = exifDataService.getExifData(file);
        assertSame(exifDataAfterRemoval, exifData);
    }

    @Test
    public void testWriteExifData() throws IOException {
        File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File file = new File(tempFolder, "NikonD60.jpg");
        Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ExifData exifData = exifDataService.getExifData(file);
        assertNotNull(exifData);
        exifData.setCameraModel("New fancy camera");
        exifData.setDescription("some new description");
        exifDataService.setExifData(file, exifData);
        ExifData newExifData = exifDataService.getExifData(file);
        assertEquals(newExifData, exifData);
    }
    
    @Test
    public void testCache() throws IOException {
        File imageFile1 = new File(tempFolder, "2018/07/2018-07-24--12-13-18---c4441d90541d557f313b17fc8d1c5d3909175040.jpg");
        TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFile1.getName(), imageFile1);
        File imageFile2 = new File(tempFolder, "2018/10/2018-10-01--19-28-17---7fdc4d56e5f86e31347599e326a6c33d64ee11ec.jpg");
        TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFile2.getName(), imageFile2);
        File imageFile3 = new File(tempFolder, "2018/09/2018-09-30--15-43-45---affed2a8d701ecae9934e5c21135963234f409b6.jpg");
        TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFile3.getName(), imageFile3);
        exifDataService.fillCacheFromArchive(tempFolder + "/*/*/*.jpg");
        assertEquals(3, exifDataService.getCacheSize());
        imageFile1.delete();
        imageFile2.delete();
        imageFile3.delete();
        assertNotNull(exifDataService.getExifData(imageFile1));
        assertNotNull(exifDataService.getExifData(imageFile2));
        assertNotNull(exifDataService.getExifData(imageFile3));
    }
    
}

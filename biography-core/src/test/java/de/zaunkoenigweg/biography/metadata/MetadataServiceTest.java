package de.zaunkoenigweg.biography.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;

import de.zaunkoenigweg.biography.metadata.exif.ExifData;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;

// TODO tests for missing methods of service (see out-commented section at end of file)
public class MetadataServiceTest {

    private static final String METADATA_JSON = "{\"dateTimeOriginal\":\"2005-02-05T15:27:33.123\",\"description\":\"Cathedral Square in Christchurch\",\"albums\":[{\"title\":\"NZ 2005\"},{\"title\":\"NZ 2007\"}]}";
    private static final BiographyMetadata METADATA = BiographyMetadata.from(METADATA_JSON);
    private ExifDataService exifDataService;
    private MetadataService sut;
    private File someFolder;
    
    @Before
    public void setUp() throws IOException {
        someFolder = Files.createTempDirectory("someFolder").toFile();
        someFolder.deleteOnExit();
        exifDataService = new ExifDataService(this.someFolder);
        this.sut = new MetadataService(exifDataService);
    }

    @Test
    public void testIsExifDataConsistentToMetadataYes() throws IOException {
        File jpgFile = new File(getClass().getResource("/metadatatest/ImageWithConsistentMetadata.jpg").getFile());

        BiographyMetadata metadata = this.sut.readMetadataFromExif(jpgFile);
        
        assertNotNull(metadata);
        assertEquals(METADATA_JSON, metadata.toJson());
        assertTrue(this.sut.isExifDataConsistentToMetadata(jpgFile, metadata));
    }

    @Test
    public void testIsExifDataConsistentToMetadataNo() throws IOException {
        File jpgFile = new File(getClass().getResource("/metadatatest/ImageWithInconsistentMetadata.jpg").getFile());

        BiographyMetadata metadata = this.sut.readMetadataFromExif(jpgFile);
        
        assertNotNull(metadata);
        assertEquals(METADATA_JSON, metadata.toJson());
        assertFalse(this.sut.isExifDataConsistentToMetadata(jpgFile, metadata));
    }

    @Test
    public void testWriteMetadataIntoExif() throws IOException {
        File sourceFile = new File(getClass().getResource("/metadatatest/NikonD60.jpg").getFile());
        File jpgFile = new File(someFolder, "NikonD60.jpg");
        Files.copy(sourceFile.toPath(), jpgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        this.sut.writeMetadataIntoExif(jpgFile, METADATA);

        BiographyMetadata metadata = this.sut.readMetadataFromExif(jpgFile);
        
        assertNotNull(metadata);
        assertEquals(METADATA_JSON, metadata.toJson());
        
        ExifData exifData = exifDataService.readExifData(jpgFile);
        assertEquals(metadata.getDescription(), exifData.getDescription().get());
        assertEquals(metadata.getDateTimeOriginal(), exifData.getDateTimeOriginal());
        
        jpgFile.delete();
    }

//    @Test
//    public void testWriteMetadataForMovie() throws IOException {
//        File fileSource = new File(getClass().getResource("/exifdatatest/2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov").getFile());
//        File movieFile = new File(someFolder, "2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov");
//        Files.copy(fileSource.toPath(), movieFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        
//        this.sut.setMetadata(movieFile, METADATA);
//        
//        File jsonFile = new File(someFolder, String.format("b%s.json", BiographyFileUtils.sha1(movieFile)));
//        String jsonFromFile = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
//        
//        assertTrue(jsonFile.exists());
//        assertEquals(METADATA_JSON, jsonFromFile);
//        
//        jsonFile.delete();
//        movieFile.delete();
//    }
//
//    @Test
//    public void testWriteMetadataForUnknownMediaFile() throws IOException {
//        File fileSource = new File(getClass().getResource("/exifdatatest/2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov").getFile());
//        File unknownMediaFile = new File(someFolder, "file.wtf");
//        Files.copy(fileSource.toPath(), unknownMediaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        
//        this.sut.setMetadata(unknownMediaFile, METADATA);
//
//        assertTrue(FileUtils.listFiles(someFolder, new String[] {"json"}, false).isEmpty());
//        
//        unknownMediaFile.delete();
//    }
//
//    @Test
//    public void testReadMetadataForMovie() throws IOException {
//        File fileSource = new File(getClass().getResource("/exifdatatest/2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov").getFile());
//        File movieFile = new File(someFolder, "2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov");
//        Files.copy(fileSource.toPath(), movieFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        
//        File jsonFile = new File(someFolder, String.format("b%s.json", BiographyFileUtils.sha1(movieFile)));
//        FileUtils.write(jsonFile, METADATA_JSON, StandardCharsets.UTF_8);
//
//        BiographyMetadata metadata = this.sut.getMetadata(movieFile);
//
//        assertNotNull(metadata);
//        assertEquals(METADATA_JSON, metadata.toJson());
//        
//        jsonFile.delete();
//        movieFile.delete();
//    }
}

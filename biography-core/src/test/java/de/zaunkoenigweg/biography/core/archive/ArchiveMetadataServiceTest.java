package de.zaunkoenigweg.biography.core.archive;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import de.zaunkoenigweg.biography.core.test.TestUtil;
import de.zaunkoenigweg.biography.metadata.MetadataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;

public class ArchiveMetadataServiceTest {

  private ArchiveMetadataService sut;
  private MetadataService metadataService;
  private File archiveFolder;

  @Before
  public void setUp() throws Exception {
    archiveFolder = Files.createTempDirectory("archiveFolder").toFile();
    archiveFolder.deleteOnExit();
    metadataService = new MetadataService(new ExifDataService());
    sut = new ArchiveMetadataService(metadataService, new ArchiveValidationService(metadataService, new ExifDataService(), archiveFolder));
  }

  /*
   * During the imports of newer files (October 2018) I recognized an encoding
   * fuckup which I could not clarify. It had somehow to do with the encoding and
   * using special chars. These tests provide examples...
   */

  @Test
  public void testMetadataEncodingMayhem1() throws Exception {
    String imageFileName = "2018-09-30--15-43-45---affed2a8d701ecae9934e5c21135963234f409b6.jpg";
    File imageFile = new File(archiveFolder, "2018/09/" + imageFileName);
    TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFileName, imageFile);
    this.sut.setDescription(imageFile, "This text uses some special characterß");
    assertTrue(metadataService.isExifDataConsistentToMetadata(imageFile, metadataService.readMetadataFromExif(imageFile)));
  }

  @Test
  public void testMetadataEncodingMayhem2() throws Exception {
    String imageFileName = "2018-10-01--19-28-17---7fdc4d56e5f86e31347599e326a6c33d64ee11ec.jpg";
    File imageFile = new File(archiveFolder, "2018/10/" + imageFileName);
    TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFileName, imageFile);
    this.sut.setDescription(imageFile, "This text uses some special characterß");
    assertTrue(metadataService.isExifDataConsistentToMetadata(imageFile, metadataService.readMetadataFromExif(imageFile)));
  }

  @Test
  public void testMetadataEncodingMayhem3() throws Exception {
    String imageFileName = "2018-07-24--12-13-18---c4441d90541d557f313b17fc8d1c5d3909175040.jpg";
    File imageFile = new File(archiveFolder, "2018/07/" + imageFileName);
    TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFileName, imageFile);
    this.sut.setDescription(imageFile, "This text uses some special characterß");
    assertTrue(metadataService.isExifDataConsistentToMetadata(imageFile, metadataService.readMetadataFromExif(imageFile)));
  }

  @Test
  public void testMetadataEncodingMayhem4() throws Exception {
    String imageFileName = "2018-09-30--15-43-45---affed2a8d701ecae9934e5c21135963234f409b6.jpg";
    File imageFile = new File(archiveFolder, "2018/09/" + imageFileName);
    TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFileName, imageFile);
    this.sut.setDescription(imageFile, "a");
    assertTrue(metadataService.isExifDataConsistentToMetadata(imageFile, metadataService.readMetadataFromExif(imageFile)));
  }

  @Test
  public void testMetadataEncodingMayhem5() throws Exception {
    String imageFileName = "2018-10-01--19-28-17---7fdc4d56e5f86e31347599e326a6c33d64ee11ec.jpg";
    File imageFile = new File(archiveFolder, "2018/10/" + imageFileName);
    TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFileName, imageFile);
    this.sut.setDescription(imageFile, "a");
    assertTrue(metadataService.isExifDataConsistentToMetadata(imageFile, metadataService.readMetadataFromExif(imageFile)));
  }

  @Test
  public void testMetadataEncodingMayhem6() throws Exception {
    String imageFileName = "2018-07-24--12-13-18---c4441d90541d557f313b17fc8d1c5d3909175040.jpg";
    File imageFile = new File(archiveFolder, "2018/07/" + imageFileName);
    TestUtil.copyFromResources("/archivemetadataservicetest/" + imageFileName, imageFile);
    this.sut.setDescription(imageFile, "a");
    assertTrue(metadataService.isExifDataConsistentToMetadata(imageFile, metadataService.readMetadataFromExif(imageFile)));
  }

}

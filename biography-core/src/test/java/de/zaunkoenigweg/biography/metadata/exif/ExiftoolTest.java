package de.zaunkoenigweg.biography.metadata.exif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class ExiftoolTest {

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

  @Test(expected = IllegalArgumentException.class)
  public void testReadAllParamsNull() {
    Exiftool.read(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadFieldsNull() {
    Exiftool.read(someEmptyFile, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadFileNull() {
    Exiftool.read(null, fieldSet(Exif.DATETIME_ORIGINAL));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadFieldSetEmpty() {
    Exiftool.read(someEmptyFile, fieldSet());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadNonExistingFile() {
    Exiftool.read(notExistingFile, fieldSet(Exif.DATETIME_ORIGINAL));
  }

  @Test(expected = IllegalStateException.class)
  public void testReadFileHasNoExifData() {
    Exiftool.read(someEmptyFile, fieldSet(Exif.DATETIME_ORIGINAL));
  }

  @Test
  public void testReadIPhone5Photo() {
    File file = new File(getClass().getResource("/exifdatatest/iPhone5s.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.DATETIME_ORIGINAL, Exif.SUBSEC_TIME_ORIGINAL));
    assertNotNull(exifData);
    assertTrue(exifData.containsKey(Exif.DATETIME_ORIGINAL));
    assertEquals("2016:07:20 21:04:13", exifData.get(Exif.DATETIME_ORIGINAL));
    assertTrue(exifData.containsKey(Exif.SUBSEC_TIME_ORIGINAL));
    assertEquals("660", exifData.get(Exif.SUBSEC_TIME_ORIGINAL));
  }

  @Test
  public void testReadJpegWithoutDatetimeOriginal() {
    File file = new File(getClass().getResource("/exifdatatest/NoDatetimeOriginal.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.DATETIME_ORIGINAL));
    assertFalse(exifData.containsKey(Exif.DATETIME_ORIGINAL));
  }

  @Test
  public void testReadCasioQV5700Photo() {
    File file = new File(getClass().getResource("/exifdatatest/CasioQV5700.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.DATETIME_ORIGINAL));
    assertNotNull(exifData);
    assertTrue(exifData.containsKey(Exif.DATETIME_ORIGINAL));
    assertEquals("2003:04:08 19:10:57", exifData.get(Exif.DATETIME_ORIGINAL));
  }

  @Test
  public void testReadNikonD60Photo() {
    File file = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.DATETIME_ORIGINAL, Exif.SUBSEC_TIME_ORIGINAL));
    assertNotNull(exifData);
    assertTrue(exifData.containsKey(Exif.DATETIME_ORIGINAL));
    assertEquals("2005:02:22 13:51:32", exifData.get(Exif.DATETIME_ORIGINAL));
    assertTrue(exifData.containsKey(Exif.SUBSEC_TIME_ORIGINAL));
    assertEquals("80", exifData.get(Exif.SUBSEC_TIME_ORIGINAL));
  }

  @Test
  public void testReadCameraMakeModel() {
    File file = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.CAMERA_MAKE, Exif.CAMERA_MODEL));
    assertNotNull(exifData);
    assertTrue(exifData.containsKey(Exif.CAMERA_MAKE));
    assertEquals("NIKON CORPORATION", exifData.get(Exif.CAMERA_MAKE));
    assertTrue(exifData.containsKey(Exif.CAMERA_MODEL));
    assertEquals("NIKON D70", exifData.get(Exif.CAMERA_MODEL));
  }

  @Test
  public void testReadJpegDescription() {
    File file = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertTrue(exifData.containsKey(Exif.IMAGE_DESCRIPTION));
    assertEquals("Christchurch; auf dem Cathedral Square", exifData.get(Exif.IMAGE_DESCRIPTION));
  }

  @Test
  public void testReadJpegWithoutDescription() {
    File file = new File(getClass().getResource("/exifdatatest/iPhone5s.jpg").getFile());
    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertTrue(exifData.isEmpty());
    assertFalse(exifData.containsKey(Exif.IMAGE_DESCRIPTION));
  }

  @Test
  public void testWriteJpegDescription() throws IOException {
    File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    File file = new File(someFolder, "ImageWithDescription.jpg");
    Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    String description = String.format("description set at %s", LocalDateTime.now());

    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertNotEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.IMAGE_DESCRIPTION, description);
    Exiftool.write(file, values);

    exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));
  }

  @Test
  public void testWriteJpegDescriptionWithUmlaut() throws IOException {
    File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    File file = new File(someFolder, "ImageWithDescription.jpg");
    Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    String description = "Christchurch, auf der S\u00fcdinsel";

    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertNotEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.IMAGE_DESCRIPTION, description);
    Exiftool.write(file, values);

    exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));
  }

  @Test
  public void testWriteJpegDescriptionWithQuotes() throws IOException {
    File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    File file = new File(someFolder, "ImageWithDescription.jpg");
    Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    String description = "Christchurch, auf der \"S\u00fcdinsel\"";

    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertNotEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.IMAGE_DESCRIPTION, description);
    Exiftool.write(file, values);

    exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));
  }

  @Test
  public void testWriteJpegDescriptionWithColon() throws IOException {
    File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    File file = new File(someFolder, "ImageWithDescription.jpg");
    Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    String description = "Christchurch: Suedinsel";

    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertNotEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.IMAGE_DESCRIPTION, description);
    Exiftool.write(file, values);

    exifData = Exiftool.read(file, fieldSet(Exif.IMAGE_DESCRIPTION));
    assertNotNull(exifData);
    assertEquals(description, exifData.get(Exif.IMAGE_DESCRIPTION));
  }

  @Test
  public void testWriteJpegLongUserComment() throws IOException {
    File fileSource = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
    File file = new File(someFolder, "ImageWithDescription.jpg");
    Files.copy(fileSource.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    StringBuilder longCommentBuilder = new StringBuilder();
    IntStream.range(1, 7001).forEach(longCommentBuilder::append);
    String userComment = longCommentBuilder.toString();

    Map<Exif, String> exifData = Exiftool.read(file, fieldSet(Exif.USER_COMMENT));
    assertNotNull(exifData);
    assertTrue(exifData.isEmpty());

    Map<Exif, String> values = new HashMap<>();
    values.put(Exif.USER_COMMENT, userComment);
    Exiftool.write(file, values);

    exifData = Exiftool.read(file, fieldSet(Exif.USER_COMMENT));
    assertNotNull(exifData);
    assertEquals(userComment, exifData.get(Exif.USER_COMMENT));
  }

  private Set<Exif> fieldSet(Exif... exifFields) {
    return Sets.newHashSet(exifFields);
  }

}

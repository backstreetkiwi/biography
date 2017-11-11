package de.zaunkoenigweg.biography.core.archive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.test.TestUtil;
import de.zaunkoenigweg.biography.metadata.MetadataService;

public class ArchiveValidationServiceTest {
	
	private ArchiveValidationService sut;
	private File archiveFolder;

	@Before
	public void setUp() throws Exception {
	    File importFolder;
	    importFolder = Files.createTempDirectory("importFolder").toFile();
	    importFolder.deleteOnExit();
	    archiveFolder = Files.createTempDirectory("archiveFolder").toFile();
	    archiveFolder.deleteOnExit();
		sut = new ArchiveValidationService();
		sut.config = new BiographyConfig();
		sut.config.setArchiveFolderProperty(archiveFolder.getAbsolutePath());
		sut.config.setImportFolderProperty(importFolder.getAbsolutePath());
		sut.config.init();
		sut.metadataService = new MetadataService();
	}

	@Test(expected=NullPointerException.class)
	public void testHasMediaFileNameNull() {
		sut.hasMediaFileName(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHasMediaFileNameFileDoesNotExist() {
		sut.hasMediaFileName(new File(archiveFolder, "notexisting.jpg"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHasMediaFileNameFileIsADirectory() {
		sut.hasMediaFileName(archiveFolder);
	}

	@Test
	public void testHasMediaFileName() {
        File fileWithMediaFileName = new File(getClass().getResource("/archivevalidationservicetest/2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg").getFile());
        File fileWithoutMediaFileName = new File(getClass().getResource("/archivevalidationservicetest/someimage.jpg").getFile());
		assertTrue(sut.hasMediaFileName(fileWithMediaFileName));
		assertFalse(sut.hasMediaFileName(fileWithoutMediaFileName));
	}

	@Test(expected=NullPointerException.class)
	public void testIsInCorrectArchiveFolderNull() {
		sut.isInCorrectArchiveFolder(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIsInCorrectArchiveFolderFileDoesNotExist() {
		sut.isInCorrectArchiveFolder(new File(archiveFolder, "notexisting.jpg"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIsInCorrectArchiveFolderFileIsADirectory() {
		sut.isInCorrectArchiveFolder(archiveFolder);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIsInCorrectArchiveFolderFileHasNoValidMediaFileName() {
        File fileWithoutMediaFileName = new File(getClass().getResource("/archivevalidationservicetest/someimage.jpg").getFile());
		sut.isInCorrectArchiveFolder(fileWithoutMediaFileName);
	}

	@Test
	public void testIsInCorrectArchiveFolder() throws IOException {
		String imageFileName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileInCorrectFolder = new File(archiveFolder, "2017/10/" + imageFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFileInCorrectFolder);
		File imageFileInWrongFolder = new File(archiveFolder, "2017/09/" + imageFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFileInWrongFolder);
		assertTrue(sut.isInCorrectArchiveFolder(imageFileInCorrectFolder));
		assertFalse(sut.isInCorrectArchiveFolder(imageFileInWrongFolder));
	}

	@Test(expected=NullPointerException.class)
	public void testHasMetadataNull() {
		sut.hasMetadata(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHasMetadataFileDoesNotExist() {
		sut.hasMetadata(new File(archiveFolder, "notexisting.jpg"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHasMetadataFileIsADirectory() {
		sut.hasMetadata(archiveFolder);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHasMetadataFileHasNoValidMediaFileName() {
        File fileWithoutMediaFileName = new File(getClass().getResource("/archivevalidationservicetest/someimage.jpg").getFile());
		sut.hasMetadata(fileWithoutMediaFileName);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testHasMetadataFileIsNotInCorrectArchiveFolder() throws IOException {
		String imageFileName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileInWrongFolder = new File(archiveFolder, "2017/09/" + imageFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFileInWrongFolder);
        sut.hasMetadata(imageFileInWrongFolder);
	}

	@Test
	public void testHasMetadata() throws IOException {
		String imageFileWithoutMetadataName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileWithoutMetadata = new File(archiveFolder, "2017/10/" + imageFileWithoutMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithoutMetadataName, imageFileWithoutMetadata);
		assertFalse(sut.hasMetadata(imageFileWithoutMetadata));

		String imageFileWithMetadataName = "2017-05-12--21-25-08---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFileWithMetadata = new File(archiveFolder, "2017/05/" + imageFileWithMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithMetadataName, imageFileWithMetadata);
		assertTrue(sut.hasMetadata(imageFileWithMetadata));

		String movieFileWithoutMetadataName = "2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithoutMetadata = new File(archiveFolder, "2017/09/" + movieFileWithoutMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithoutMetadataName, movieFileWithoutMetadata);
		assertFalse(sut.hasMetadata(movieFileWithoutMetadata));
		
		String movieFileWithMetadataName = "2017-08-20--18-39-22---239acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithMetadata = new File(archiveFolder, "2017/08/" + movieFileWithMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithMetadataName, movieFileWithMetadata);
		String metadataFileName = "b239acab5f70c6ecafa73634a9cb6885b1835ce5c.json";
		File metadataFile = new File(archiveFolder, "2017/08/" + metadataFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + metadataFileName, metadataFile);
		assertTrue(sut.hasMetadata(movieFileWithMetadata));
	}
	
	@Test(expected=NullPointerException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameNull() {
		sut.doesMetadataDatetimeOriginalMatchFilename(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameFileDoesNotExist() {
		sut.doesMetadataDatetimeOriginalMatchFilename(new File(archiveFolder, "notexisting.jpg"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameFileIsADirectory() {
		sut.doesMetadataDatetimeOriginalMatchFilename(archiveFolder);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameFileHasNoValidMediaFileName() {
        File fileWithoutMediaFileName = new File(getClass().getResource("/archivevalidationservicetest/someimage.jpg").getFile());
		sut.doesMetadataDatetimeOriginalMatchFilename(fileWithoutMediaFileName);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameFileIsNotInCorrectArchiveFolder() throws IOException {
		String imageFileName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileInWrongFolder = new File(archiveFolder, "2017/09/" + imageFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFileInWrongFolder);
        sut.doesMetadataDatetimeOriginalMatchFilename(imageFileInWrongFolder);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameFileHasNoMetadataInExif() throws IOException {
		String imageFileWithoutMetadataName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileWithoutMetadata = new File(archiveFolder, "2017/10/" + imageFileWithoutMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithoutMetadataName, imageFileWithoutMetadata);
        sut.doesMetadataDatetimeOriginalMatchFilename(imageFileWithoutMetadata);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataDatetimeOriginalMatchFilenameFileHasNoMetadataInJson() throws IOException {
		String movieFileWithoutMetadataName = "2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithoutMetadata = new File(archiveFolder, "2017/09/" + movieFileWithoutMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithoutMetadataName, movieFileWithoutMetadata);
        sut.doesMetadataDatetimeOriginalMatchFilename(movieFileWithoutMetadata);
	}

	@Test
	public void testDoesMetadataDatetimeOriginalMatchFilename() throws IOException {
		String imageFileWithMatchingMetadataName = "2017-05-12--21-25-08---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFileWithMatchingMetadata = new File(archiveFolder, "2017/05/" + imageFileWithMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithMatchingMetadataName, imageFileWithMatchingMetadata);
		assertTrue(sut.doesMetadataDatetimeOriginalMatchFilename(imageFileWithMatchingMetadata));

		String imageFileWithNonMatchingMetadataName = "2017-05-12--20-00-00---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFileWithNonMatchingMetadata = new File(archiveFolder, "2017/05/" + imageFileWithNonMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithNonMatchingMetadataName, imageFileWithNonMatchingMetadata);
		assertFalse(sut.doesMetadataDatetimeOriginalMatchFilename(imageFileWithNonMatchingMetadata));
		
		String movieFileWithMatchingMetadataName = "2017-08-20--18-39-22---239acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithMatchingMetadata = new File(archiveFolder, "2017/08/" + movieFileWithMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithMatchingMetadataName, movieFileWithMatchingMetadata);
		String matchingMetadataFileName = "b239acab5f70c6ecafa73634a9cb6885b1835ce5c.json";
		File matchingMetadataFile = new File(archiveFolder, "2017/08/" + matchingMetadataFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + matchingMetadataFileName, matchingMetadataFile);
		assertTrue(sut.doesMetadataDatetimeOriginalMatchFilename(movieFileWithMatchingMetadata));
		
		String movieFileWithoutMatchingMetadataName = "2017-08-20--18-00-00---339acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithoutMatchingMetadata = new File(archiveFolder, "2017/08/" + movieFileWithoutMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithoutMatchingMetadataName, movieFileWithoutMatchingMetadata);
		String nonMatchingMetadataFileName = "b339acab5f70c6ecafa73634a9cb6885b1835ce5c.json";
		File nonMatchingMetadataFile = new File(archiveFolder, "2017/08/" + nonMatchingMetadataFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + nonMatchingMetadataFileName, nonMatchingMetadataFile);
		assertFalse(sut.doesMetadataDatetimeOriginalMatchFilename(movieFileWithoutMatchingMetadata));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDoesMetadataMatchExifDataMetadataDatetimeOriginalDoesNotMatchFilename() throws IOException {
		String imageFileWithNonMatchingMetadataName = "2017-05-12--20-00-00---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFileWithNonMatchingMetadata = new File(archiveFolder, "2017/05/" + imageFileWithNonMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithNonMatchingMetadataName, imageFileWithNonMatchingMetadata);
        sut.doesMetadataMatchExifData(imageFileWithNonMatchingMetadata);
	}

	@Test
	public void testDoesMetadataMatchExifData() throws IOException {
		String movieFileName = "2017-08-20--18-39-22---239acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFile = new File(archiveFolder, "2017/08/" + movieFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileName, movieFile);
		String metadataFileName = "b239acab5f70c6ecafa73634a9cb6885b1835ce5c.json";
		File metadataFile = new File(archiveFolder, "2017/08/" + metadataFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + metadataFileName, metadataFile);
        assertTrue(sut.doesMetadataMatchExifData(movieFile));

		String imageFileName = "2017-05-12--21-25-08---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFile = new File(archiveFolder, "2017/05/" + imageFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFile);
        assertTrue(sut.doesMetadataMatchExifData(imageFile));

		String imageFileNotMatchingDescriptionName = "2017-04-03--20-58-09---5b9732ccc64bddf285dae030276639fc367d4e14.jpg";
		File imageFileNotMatchingDescription = new File(archiveFolder, "2017/04/" + imageFileNotMatchingDescriptionName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileNotMatchingDescriptionName, imageFileNotMatchingDescription);
        assertFalse(sut.doesMetadataMatchExifData(imageFileNotMatchingDescription));
	}

}

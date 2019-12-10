package de.zaunkoenigweg.biography.core.archivemetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import de.zaunkoenigweg.biography.core.archive.Archive;
import de.zaunkoenigweg.biography.core.test.TestUtil;
import de.zaunkoenigweg.biography.metadata.MetadataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;

public class ArchiveValidationServiceTest {
	
	private ArchiveValidationService sut;
	private File archiveFolder;

	@Before
	public void setUp() throws Exception {
	    archiveFolder = Files.createTempDirectory("archiveFolder").toFile();
	    archiveFolder.deleteOnExit();
		sut = new ArchiveValidationService(new MetadataService(new ExifDataService(new Archive(this.archiveFolder))), new ExifDataService(new Archive(this.archiveFolder)), archiveFolder);
	}

	@Test(expected=NullPointerException.class)
	public void testValidateFileNameIsNull() {
		sut.validate(null);
	}

	@Test
	public void testValidateFileDoesNotExist() {
		assertEquals(ArchiveValidationService.ValidationResult.FILE_DOES_NOT_EXIST, sut.validate(new File(archiveFolder, "notexisting.jpg")));
	}

	@Test
	public void testValidateFileIsADirectory() {
		assertEquals(ArchiveValidationService.ValidationResult.FILE_DOES_NOT_EXIST, sut.validate(archiveFolder));
	}

	@Test
	public void testValidateFilename() {
        File fileWithoutMediaFileName = new File(getClass().getResource("/archivevalidationservicetest/someimage.jpg").getFile());
		assertEquals(ArchiveValidationService.ValidationResult.FILENAME_NOT_VALID, sut.validate(fileWithoutMediaFileName));
	}

	@Test
	public void testValidateFileIsNotInCorrectArchiveFolder() throws IOException {
		String imageFileName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileInWrongFolder = new File(archiveFolder, "2017/09/" + imageFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFileInWrongFolder);
		assertEquals(ArchiveValidationService.ValidationResult.FILE_IS_NOT_IN_CORRECT_ARCHIVE_FILDER, sut.validate(imageFileInWrongFolder));
	}

	@Test
	public void testValidataFileHasNoMetadata() throws IOException {
		String imageFileWithoutMetadataName = "2017-10-17--16-58-40---7da47dd317e0e74388fa9688c1c7481ab614ce2f.jpg";
		File imageFileWithoutMetadata = new File(archiveFolder, "2017/10/" + imageFileWithoutMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithoutMetadataName, imageFileWithoutMetadata);
        assertEquals(ArchiveValidationService.ValidationResult.FILE_HAS_NO_METADATA, sut.validate(imageFileWithoutMetadata));

		String movieFileWithoutMetadataName = "2017-09-21--18-39-22---139acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithoutMetadata = new File(archiveFolder, "2017/09/" + movieFileWithoutMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithoutMetadataName, movieFileWithoutMetadata);
        assertEquals(ArchiveValidationService.ValidationResult.FILE_HAS_NO_METADATA, sut.validate(movieFileWithoutMetadata));
	}
	
	@Test
	public void testValidateDatetimeOriginalInconsistent() throws IOException {
		String imageFileWithNonMatchingMetadataName = "2017-05-12--20-00-00---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFileWithNonMatchingMetadata = new File(archiveFolder, "2017/05/" + imageFileWithNonMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileWithNonMatchingMetadataName, imageFileWithNonMatchingMetadata);
        assertEquals(ArchiveValidationService.ValidationResult.DATETIME_ORIGINAL_INCONSISTENT, sut.validate(imageFileWithNonMatchingMetadata));
		
		String movieFileWithoutMatchingMetadataName = "2017-08-20--18-00-00---339acab5f70c6ecafa73634a9cb6885b1835ce5c.mov";
		File movieFileWithoutMatchingMetadata = new File(archiveFolder, "2017/08/" + movieFileWithoutMatchingMetadataName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + movieFileWithoutMatchingMetadataName, movieFileWithoutMatchingMetadata);
		String nonMatchingMetadataFileName = "b339acab5f70c6ecafa73634a9cb6885b1835ce5c.json";
		File nonMatchingMetadataFile = new File(archiveFolder, "2017/08/" + nonMatchingMetadataFileName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + nonMatchingMetadataFileName, nonMatchingMetadataFile);
        assertEquals(ArchiveValidationService.ValidationResult.DATETIME_ORIGINAL_INCONSISTENT, sut.validate(movieFileWithoutMatchingMetadata));
	}

	@Test
	public void testValidateMetadataInconsistent() throws IOException {
		String imageFileNotMatchingDescriptionName = "2017-04-03--20-58-09---5b9732ccc64bddf285dae030276639fc367d4e14.jpg";
		File imageFileNotMatchingDescription = new File(archiveFolder, "2017/04/" + imageFileNotMatchingDescriptionName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileNotMatchingDescriptionName, imageFileNotMatchingDescription);
        assertEquals(ArchiveValidationService.ValidationResult.METADATA_INCONSISTENT, sut.validate(imageFileNotMatchingDescription));
	}

	@Test
	public void testValidateHashcodeInconsistent() throws IOException {
		String imageFileNotMatchingHashcodeName = "2017-05-12--21-25-08---affeaffeaffeaffeaffeaffeaffeaffeaffeaffe.jpg";
		File imageFileNotMatchingHashcode = new File(archiveFolder, "2017/05/" + imageFileNotMatchingHashcodeName);
        TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileNotMatchingHashcodeName, imageFileNotMatchingHashcode);
        assertEquals(ArchiveValidationService.ValidationResult.HASHCODE_INCONSISTENT, sut.validate(imageFileNotMatchingHashcode));
	}
	
	@Test
	public void testValidate() throws IOException {
		String imageFileName = "2017-05-12--21-25-08---2d1d6ced6c4df4f018f10f7697e9453fc1cfc86f.jpg";
		File imageFile = new File(archiveFolder, "2017/05/" + imageFileName);
	    TestUtil.copyFromResources("/archivevalidationservicetest/" + imageFileName, imageFile);
        assertEquals(ArchiveValidationService.ValidationResult.OK, sut.validate(imageFile));
	}

}

package de.zaunkoenigweg.biography.core.config;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

public class BiographyConfigTest {

	private BiographyConfig sut;
    private File existingImportFolder;
    private File nonExistingImportFolder;
    private File existingArchiveFolder;
    private File nonExistingArchiveFolder;

    @Before
    public void setUp() throws IOException {
    	System.clearProperty(BiographyConfig.KEY_IMPORT_FOLDER);
    	System.clearProperty(BiographyConfig.KEY_ARCHIVE_FOLDER);
        existingImportFolder = Files.createTempDirectory("biographyImportFolder").toFile();
        existingImportFolder.deleteOnExit();
        nonExistingImportFolder = new File("./thisfolderdoesnotexistA");
        existingArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        existingArchiveFolder.deleteOnExit();
        nonExistingArchiveFolder = new File("./thisfolderdoesnotexistB");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConfigNoPropertiesSet() {
        sut = new BiographyConfig();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNoArchiveFolderSet() {
    	System.setProperty(BiographyConfig.KEY_IMPORT_FOLDER, existingImportFolder.getAbsolutePath());
        sut = new BiographyConfig();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testArchiveFolderNotExisting() {
    	System.setProperty(BiographyConfig.KEY_IMPORT_FOLDER, existingImportFolder.getAbsolutePath());
    	System.setProperty(BiographyConfig.KEY_ARCHIVE_FOLDER, nonExistingArchiveFolder.getAbsolutePath());
        sut = new BiographyConfig();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNoImportFolderSet() {
    	System.setProperty(BiographyConfig.KEY_ARCHIVE_FOLDER, existingArchiveFolder.getAbsolutePath());
        sut = new BiographyConfig();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testImportFolderNotExisting() {
    	System.setProperty(BiographyConfig.KEY_ARCHIVE_FOLDER, existingArchiveFolder.getAbsolutePath());
    	System.setProperty(BiographyConfig.KEY_IMPORT_FOLDER, nonExistingImportFolder.getAbsolutePath());
        sut = new BiographyConfig();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testImportFolderSameAsArchiveFolder() {
    	System.setProperty(BiographyConfig.KEY_IMPORT_FOLDER, existingImportFolder.getAbsolutePath());
    	System.setProperty(BiographyConfig.KEY_ARCHIVE_FOLDER, existingImportFolder.getAbsolutePath());
        sut = new BiographyConfig();
    }
    
    @Test
    public void testAllSet() {
    	System.setProperty(BiographyConfig.KEY_IMPORT_FOLDER, existingImportFolder.getAbsolutePath());
    	System.setProperty(BiographyConfig.KEY_ARCHIVE_FOLDER, existingArchiveFolder.getAbsolutePath());
        sut = new BiographyConfig();
        assertNotNull(sut);
    }
    
}

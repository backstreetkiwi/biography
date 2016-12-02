package de.zaunkoenigweg.biography.core.config;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

public class BiographyConfigTest {

	private BiographyConfig sut;
    private File existingImportFolder;
    private File nonExistingImportFolder;
    private File existingArchiveFolder;
    private File nonExistingArchiveFolder;
    private File existingIndexFolder;
    private File nonExistingIndexFolder;

    @Before
    public void setUp() throws IOException {
        existingImportFolder = Files.createTempDirectory("biographyImportFolder").toFile();
        existingImportFolder.deleteOnExit();
        nonExistingImportFolder = new File("./thisfolderdoesnotexistA");
        existingArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        existingArchiveFolder.deleteOnExit();
        nonExistingArchiveFolder = new File("./thisfolderdoesnotexistB");
        existingIndexFolder = Files.createTempDirectory("biographyIndexFolder").toFile();
        existingIndexFolder.deleteOnExit();
        nonExistingIndexFolder = new File("./thisfolderdoesnotexistC");
    }

    @Test(expected=BeanInitializationException.class)
    public void testConfigNoPropertiesSet() {
        sut = new BiographyConfig();
        sut.init();
    }

    @Test(expected=BeanInitializationException.class)
    public void testNoArchiveFolderSet() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testArchiveFolderNotExisting() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(nonExistingArchiveFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testNoImportFolderSet() {
        sut = new BiographyConfig();
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testImportFolderNotExisting() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(nonExistingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testImportFolderSameAsArchiveFolder() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingImportFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testIndexFolderNotSet() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testIndexFolderNotExisting() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.setIndexFolderProperty(nonExistingIndexFolder.getAbsolutePath());
        sut.init();
    }
    
    @Test
    public void testAllSet() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.setIndexFolderProperty(existingIndexFolder.getAbsolutePath());
        sut.init();
        assertNotNull(sut);
    }
    
}

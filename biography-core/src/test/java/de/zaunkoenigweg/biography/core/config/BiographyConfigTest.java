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
    private File existingFolderInsideArchiveFolder;
    private File nonExistingArchiveFolder;

    @Before
    public void setUp() throws IOException {
        existingImportFolder = Files.createTempDirectory("biographyImportFolder").toFile();
        existingImportFolder.deleteOnExit();
        nonExistingImportFolder = new File("./thisfolderdoesnotexistA");
        existingArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        existingArchiveFolder.deleteOnExit();
        existingFolderInsideArchiveFolder = new File(existingArchiveFolder, "subfolder");
        existingFolderInsideArchiveFolder.mkdirs();
        nonExistingArchiveFolder = new File("./thisfolderdoesnotexistB");
    }

    @Test(expected=BeanInitializationException.class)
    public void testConfigNoPropertiesSet() {
        sut = new BiographyConfig();
        sut.afterPropertiesSet();
    }

    @Test(expected=BeanInitializationException.class)
    public void testNoArchiveFolderSet() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.afterPropertiesSet();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testArchiveFolderNotExisting() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(nonExistingArchiveFolder.getAbsolutePath());
        sut.afterPropertiesSet();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testNoImportFolderSet() {
        sut = new BiographyConfig();
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.afterPropertiesSet();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testImportFolderNotExisting() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(nonExistingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.afterPropertiesSet();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testImportFolderSameAsArchiveFolder() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingImportFolder.getAbsolutePath());
        sut.afterPropertiesSet();
    }
    
    @Test(expected=BeanInitializationException.class)
    public void testImportFolderAndArchiveFolderHaveNoCommonParent() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingFolderInsideArchiveFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.afterPropertiesSet();
    }
    
    @Test
    public void testAllSet() {
        sut = new BiographyConfig();
        sut.setImportFolderProperty(existingImportFolder.getAbsolutePath());
        sut.setArchiveFolderProperty(existingArchiveFolder.getAbsolutePath());
        sut.afterPropertiesSet();
        assertNotNull(sut);
    }
    
}

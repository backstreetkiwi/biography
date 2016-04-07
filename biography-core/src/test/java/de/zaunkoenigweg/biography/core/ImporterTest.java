package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class ImporterTest {
    
    private Importer sut;
    private File existingImportFolder;
    private File nonExistingImportFolder;
    private File existingArchiveFolder;
    private File nonExistingArchiveFolder;
    
    @Before
    public void setUp() throws IOException {
        sut = new Importer();
        existingImportFolder = Files.createTempDirectory("biographyImportFolder").toFile();
        existingImportFolder.deleteOnExit();
        nonExistingImportFolder = new File("./thisfolderdoesnotexistA");
        existingArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        existingArchiveFolder.deleteOnExit();
        nonExistingArchiveFolder = new File("./thisfolderdoesnotexistB");
    }

    @Test(expected=RuntimeException.class)
    public void testImportAllNotInitialized() {
        sut.importAll();
    }

    @Test(expected=RuntimeException.class)
    public void testImportAllImportFolderNotInitialized() {
        sut.setArchive(existingArchiveFolder);
        sut.importAll();
    }

    @Test(expected=RuntimeException.class)
    public void testImportAllArchiveNotInitialized() {
        sut.setImportFolder(existingImportFolder);
        sut.importAll();
    }

    @Test(expected=RuntimeException.class)
    public void testImportAllImportFolderDoesNotExist() {
        sut.setArchive(existingArchiveFolder);
        sut.setImportFolder(nonExistingImportFolder);
        sut.importAll();
    }

    @Test(expected=RuntimeException.class)
    public void testImportAllArchiveDoesNotExist() {
        sut.setArchive(nonExistingArchiveFolder);
        sut.setImportFolder(existingImportFolder);
        sut.importAll();
    }

    @Test(expected=RuntimeException.class)
    public void testImportAllImportFolderEqualToArchive() {
        sut.setArchive(existingImportFolder);
        sut.setImportFolder(existingImportFolder);
        sut.importAll();
    }

    @Test
    public void testImportAll() {
        sut.setArchive(existingArchiveFolder);
        sut.setImportFolder(existingImportFolder);
        sut.importAll();
        assertNotNull(sut);
    }

}

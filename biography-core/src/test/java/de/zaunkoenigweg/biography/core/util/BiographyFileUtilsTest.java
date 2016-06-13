package de.zaunkoenigweg.biography.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BiographyFileUtilsTest {
    
    private File biographyArchiveFolder;
    private File notExistingFolder;
    private File someFile;
    
    @Before
    public void setUp() throws IOException {
        biographyArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        biographyArchiveFolder.deleteOnExit();
        notExistingFolder = new File("./thisfolderdoesnotexist");
        someFile = new File(biographyArchiveFolder, "someFile.txt");
        someFile.createNewFile();
    }

    @Test
    public void testGetMediaFoldersArchiveFolderIsNull() {
    	List<File> mediaFolders = BiographyFileUtils.getMediaFolders(null);
    	assertNotNull(mediaFolders);
    	assertEquals(0, mediaFolders.size());
    }

    @Test
    public void testGetMediaFoldersArchiveFolderIsNotExisting() {
    	List<File> mediaFolders = BiographyFileUtils.getMediaFolders(notExistingFolder);
    	assertNotNull(mediaFolders);
    	assertEquals(0, mediaFolders.size());
    }

    @Test
    public void testGetMediaFoldersArchiveFolderIsNormalFile() {
    	List<File> mediaFolders = BiographyFileUtils.getMediaFolders(someFile);
    	assertNotNull(mediaFolders);
    	assertEquals(0, mediaFolders.size());
    }

    @Test
    public void testGetMediaFolders() {
    	new File(biographyArchiveFolder, "2015/10").mkdirs();    	
    	new File(biographyArchiveFolder, "2015/11").mkdirs();    	
    	new File(biographyArchiveFolder, "2015/12").mkdirs();    	
    	new File(biographyArchiveFolder, "2016/01").mkdirs();    	
    	new File(biographyArchiveFolder, "2016/02").mkdirs();    	
    	new File(biographyArchiveFolder, "2016/03").mkdirs();    	
    	new File(biographyArchiveFolder, "2016/04").mkdirs();    	
    	new File(biographyArchiveFolder, "21/12").mkdirs();    	
    	new File(biographyArchiveFolder, "2016/13").mkdirs();    	
    	new File(biographyArchiveFolder, "2016/foo").mkdirs();    	
    	new File(biographyArchiveFolder, "bar/12").mkdirs();    	
    	new File(biographyArchiveFolder, "bar/foo").mkdirs();    	
    	List<File> mediaFolders = BiographyFileUtils.getMediaFolders(biographyArchiveFolder);
    	assertNotNull(mediaFolders);
    	assertEquals(7, mediaFolders.size());
    	assertEquals("2015", mediaFolders.get(0).getParentFile().getName());
    	assertEquals("10", mediaFolders.get(0).getName());
    	assertEquals("2015", mediaFolders.get(1).getParentFile().getName());
    	assertEquals("11", mediaFolders.get(1).getName());
    	assertEquals("2015", mediaFolders.get(2).getParentFile().getName());
    	assertEquals("12", mediaFolders.get(2).getName());
    	assertEquals("2016", mediaFolders.get(3).getParentFile().getName());
    	assertEquals("01", mediaFolders.get(3).getName());
    	assertEquals("2016", mediaFolders.get(4).getParentFile().getName());
    	assertEquals("02", mediaFolders.get(4).getName());
    	assertEquals("2016", mediaFolders.get(5).getParentFile().getName());
    	assertEquals("03", mediaFolders.get(5).getName());
    	assertEquals("2016", mediaFolders.get(6).getParentFile().getName());
    	assertEquals("04", mediaFolders.get(6).getName());
    }

}

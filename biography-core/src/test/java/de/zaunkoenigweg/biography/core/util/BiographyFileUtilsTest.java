package de.zaunkoenigweg.biography.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.zaunkoenigweg.lexi4j.exiftool.Exiftool;

public class BiographyFileUtilsTest {
    
    private File biographyArchiveFolder;
    private File notExistingFolder;
    private File notExistingFile;
    private File someFile;
    private File someFolder;
    
    @Before
    public void setUp() throws IOException {
        biographyArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        biographyArchiveFolder.deleteOnExit();
        notExistingFolder = new File("./thisfolderdoesnotexist");
        notExistingFile = new File(biographyArchiveFolder, "i-do-not-exist");
        someFile = new File(biographyArchiveFolder, "someFile.txt");
        someFile.createNewFile();
        someFolder = Files.createTempDirectory("someFolder").toFile();
        someFolder.deleteOnExit();
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
    	new File(biographyArchiveFolder, "999/99").mkdirs();    	
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

    @Test
    public void testGetMediaFiles() throws IOException {
    	File folder = null;
    	folder = new File(biographyArchiveFolder, "2016/03");    	
    	folder.mkdirs();    	
    	new File(folder, "2016-03-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	new File(folder, "2016-03-13--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	new File(folder, "2016-03-17--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	folder = new File(biographyArchiveFolder, "2016/04");    	
    	folder.mkdirs();    	
    	new File(folder, "2016-04-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	new File(folder, "2016-04-11--12-41-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	new File(folder, "2016-04-11--12-42-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	new File(folder, "2016-04-11--12-43-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg").createNewFile();
    	List<File> mediaFiles = BiographyFileUtils.getMediaFiles(biographyArchiveFolder);
    	assertNotNull(mediaFiles);
    	assertEquals(7, mediaFiles.size());
    	assertEquals("2016", mediaFiles.get(0).getParentFile().getParentFile().getName());
    	assertEquals("03", mediaFiles.get(0).getParentFile().getName());
    	assertEquals("2016-03-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(0).getName());
    	assertEquals("2016", mediaFiles.get(1).getParentFile().getParentFile().getName());
    	assertEquals("03", mediaFiles.get(1).getParentFile().getName());
    	assertEquals("2016-03-13--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(1).getName());
    	assertEquals("2016", mediaFiles.get(2).getParentFile().getParentFile().getName());
    	assertEquals("03", mediaFiles.get(2).getParentFile().getName());
    	assertEquals("2016-03-17--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(2).getName());
    	assertEquals("2016", mediaFiles.get(3).getParentFile().getParentFile().getName());
    	assertEquals("04", mediaFiles.get(3).getParentFile().getName());
    	assertEquals("2016-04-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(3).getName());
    	assertEquals("2016", mediaFiles.get(4).getParentFile().getParentFile().getName());
    	assertEquals("04", mediaFiles.get(4).getParentFile().getName());
    	assertEquals("2016-04-11--12-41-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(4).getName());
    	assertEquals("2016", mediaFiles.get(5).getParentFile().getParentFile().getName());
    	assertEquals("04", mediaFiles.get(5).getParentFile().getName());
    	assertEquals("2016-04-11--12-42-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(5).getName());
    	assertEquals("2016", mediaFiles.get(6).getParentFile().getParentFile().getName());
    	assertEquals("04", mediaFiles.get(6).getParentFile().getName());
    	assertEquals("2016-04-11--12-43-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg", mediaFiles.get(6).getName());
    }

    @Test
    public void testIsValidFilename() {
    	assertFalse(BiographyFileUtils.isMediaFileName(null));
    	assertFalse(BiographyFileUtils.isMediaFileName(new File("")));
    	assertFalse(BiographyFileUtils.isMediaFileName(new File(" ")));
    	assertFalse(BiographyFileUtils.isMediaFileName(new File("cabbage")));
    	assertFalse(BiographyFileUtils.isMediaFileName(new File("2016-05-11--12-40-14---335g45c087c9937a772a45a8e5bc755d705a5ab5.jpg")));
    	assertFalse(BiographyFileUtils.isMediaFileName(new File("2016-05-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.abc")));
    	assertTrue(BiographyFileUtils.isMediaFileName(new File("2016-05-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.jpg")));
        assertTrue(BiographyFileUtils.isMediaFileName(new File("2016-05-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.mov")));
        assertTrue(BiographyFileUtils.isMediaFileName(new File("2016-05-11--12-40-14---335f45c087c9937a772a45a8e5bc755d705a5ab5.mpg")));
    }
    
    @Test
    public void testSha1NoFile() {
    	assertNull(BiographyFileUtils.sha1(null));
    }
    
    @Test
    public void testSha1NotExistingFile() {
    	assertNull(BiographyFileUtils.sha1(notExistingFile));
    }
    
    @Test
    public void testSha1() {
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", BiographyFileUtils.sha1(new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile())));
    }

    @Test
    public void testSha1AfterExifChange() throws IOException {
        File sourceFile = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File targetFile = new File(someFolder, "ImageWithChangedDescription.jpg");
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", BiographyFileUtils.sha1(sourceFile));
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", BiographyFileUtils.sha1(targetFile));
        Exiftool.update(targetFile)
            .withImageDescription("blablabla")
            .withUserComment("blablabla")
            .perform();
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", BiographyFileUtils.sha1(targetFile));
    }

}

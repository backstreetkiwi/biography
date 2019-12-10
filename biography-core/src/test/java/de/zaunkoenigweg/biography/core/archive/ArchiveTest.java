package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ArchiveTest {
	
    private File biographyArchiveFolder;
    private File notExistingFolder;
    private File someFile;
    private File someFolder;
	private Archive sut;

	@Before
	public void setUp() throws Exception {
        biographyArchiveFolder = Files.createTempDirectory("biographyArchiveFolder").toFile();
        biographyArchiveFolder.deleteOnExit();
        notExistingFolder = new File("./thisfolderdoesnotexist");
        someFile = new File(biographyArchiveFolder, "someFile.txt");
        someFile.createNewFile();
        someFolder = Files.createTempDirectory("someFolder").toFile();
        someFolder.deleteOnExit();
		sut = new Archive(biographyArchiveFolder);
	}

	@Test(expected=NullPointerException.class)
	public void testInitArchiveWithNull() {
		new Archive(null);
	}
	
    @Test
    public void testGetMediaFoldersArchiveFolderIsNotExisting() {
    	Archive archiveInNotExistingFolder = new Archive(notExistingFolder);
    	List<File> mediaFolders = archiveInNotExistingFolder.mediaFolders();
    	assertNotNull(mediaFolders);
    	assertEquals(0, mediaFolders.size());
    }

    @Test
    public void testGetMediaFoldersArchiveFolderIsNormalFile() {
    	Archive archiveInNotExistingFolder = new Archive(someFile);
    	List<File> mediaFolders = archiveInNotExistingFolder.mediaFolders();
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
    	List<File> mediaFolders = this.sut.mediaFolders();
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
    	List<File> mediaFiles = this.sut.mediaFiles();
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
}

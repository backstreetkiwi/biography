package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.zaunkoenigweg.lexi4j.exiftool.Exiftool;

public class Sha1Test {

    private File someFolder;

    @Before
    public void setUp() throws IOException {
        someFolder = Files.createTempDirectory("someFolder").toFile();
        someFolder.deleteOnExit();
    }

    @Test
	public void testIsValid() {
		assertFalse(Sha1.isValid(null));
		assertFalse(Sha1.isValid(""));
		assertFalse(Sha1.isValid(" "));
		assertFalse(Sha1.isValid("c69239ffcc01886f9d73ecd1076271bb65c26ba41"));
		assertFalse(Sha1.isValid("c69239ffcc01886f9d73ecd1076271bb65c26bag"));
		assertTrue(Sha1.isValid("c69239ffcc01886f9d73ecd1076271bb65c26ba4"));
	}

	@Test
	public void testFactoryMethodOf() {
		Sha1 sha1 = Sha1.of("c69239ffcc01886f9d73ecd1076271bb65c26ba4");
		assertNotNull(sha1);
		assertEquals("c69239ffcc01886f9d73ecd1076271bb65c26ba4", sha1.value());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFactoryMethodOfFailure() {
		Sha1.of("hurz");
	}

    @Test
    public void testSha1NoFile() {
    	assertNull(Sha1.calculate(null));
    }
    
    @Test
    public void testSha1NotExistingFile() {
    	assertNull(Sha1.calculate(new File("i/do/not.exist")));
    }
    
    @Test
    public void testSha1() {
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", Sha1.calculate(new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile())).value());
    }

    @Test
    public void testSha1AfterExifChange() throws IOException {
        File sourceFile = new File(getClass().getResource("/exifdatatest/NikonD60.jpg").getFile());
        File targetFile = new File(someFolder, "ImageWithChangedDescription.jpg");
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", Sha1.calculate(sourceFile).value());
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", Sha1.calculate(targetFile).value());
        new Exiftool().update(targetFile)
            .withImageDescription("blablabla")
            .withUserComment("blablabla")
            .perform();
        assertEquals("7648bf4572edc4e71ed7992db4071e08b1a57597", Sha1.calculate(targetFile).value());
    }
	
}

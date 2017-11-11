package de.zaunkoenigweg.biography.core.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TestUtil {

	public static void copyFromResources(String sourcePath, File targetFile) throws IOException {
        File sourceFile = new File(TestUtil.class.getResource(sourcePath).getFile());
        Files.createDirectories(targetFile.toPath());
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
}

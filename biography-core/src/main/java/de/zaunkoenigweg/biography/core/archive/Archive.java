package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileName;

@Component
public class Archive {

    private final static Log LOG = LogFactory.getLog(Archive.class);
	
	private File archiveFolder;

	public Archive(File archiveFolder) {
		this.archiveFolder = Objects.requireNonNull(archiveFolder);
		LOG.info("Archive component initialized.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

    /**
     * Reads all media folders in baseFolder and returns a list containing all
     * media folders ordered by date, oldest folders first.
     * 
     * @return list of media folders.
     */
    public List<File> mediaFolders() {

        if (!archiveFolder.exists() || !archiveFolder.isDirectory()) {
            return Collections.emptyList();
        }

        return Arrays.stream(archiveFolder.listFiles())
                .filter(Archive::isValidYearFolder)
                .sorted()
                .map(File::listFiles)
                .flatMap(Arrays::stream)
                .filter(Archive::isValidMonthFolder)
                .sorted()
                .collect(Collectors.toList());
    }
	
    /**
     * Reads all media folders in the archive and returns a list containing all
     * files therein, ordered by date, oldest files first.
     * 
     * @return list of media files.
     */
    public List<File> mediaFiles() {

        return mediaFolders().stream()
                .map(File::listFiles)
                .flatMap(Arrays::stream)
                .filter(file -> MediaFileName.isValid(file.getName()))
                .sorted()
                .collect(Collectors.toList());
    }
    
    public File getArchiveFolder() {
		return archiveFolder;
	}

	/**
     * Does the name of the given folder represent a valid year?
     */
    private static boolean isValidYearFolder(File folder) {

        if (folder == null || !folder.isDirectory()) {
            return false;
        }

        if (!StringUtils.isNumeric(folder.getName())) {
            return false;
        }

        int number = Integer.parseInt(folder.getName());
        return (1000 <= number) && (number <= 9999);
    }

    /**
     * Does the name of the given file represent a valid month?
     */
    private static boolean isValidMonthFolder(File folder) {

        if (folder == null || !folder.isDirectory()) {
            return false;
        }

        return IntStream.range(1, 13)
                .mapToObj(number -> String.format("%02d", number))
                .anyMatch(month -> month.equals(folder.getName()));
    }
	
}

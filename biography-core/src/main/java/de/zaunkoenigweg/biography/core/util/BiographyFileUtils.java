package de.zaunkoenigweg.biography.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Collection of utils for working with files.
 * 
 * @author Nikolaus Winter
 *
 */
public class BiographyFileUtils {

    private final static Pattern FILE_FORMAT = Pattern.compile("\\d{4}-\\d{2}-\\d{2}--\\d{2}-\\d{2}-\\d{2}---\\p{XDigit}{40}.jpg");

    /**
	 * Reads all media folders in baseFolder and returns a list containing
	 * all media folders ordered by date, oldest folders first.
	 * @param baseFolder Biography base folder to search
	 * @return list of media folders.
	 */
	public static List<File> getMediaFolders(File baseFolder) {
		List<File> mediaFolders = new ArrayList<>();
		if(baseFolder==null) {
			return mediaFolders;
		}
		if(!baseFolder.exists()) {
			return mediaFolders;
		}
		if(!baseFolder.isDirectory()) {
			return mediaFolders;
		}
		
		File[] yearFolders = baseFolder.listFiles();
		Arrays.stream(yearFolders).filter(isDirectory()).filter(isFilenameYear()).sorted().forEach((yearFolder) -> {
			File[] monthFolders = yearFolder.listFiles();
			Arrays.stream(monthFolders).filter(isFilenameMonth()).sorted().forEach((monthFolder) -> {
				mediaFolders.add(monthFolder);
			});
		});
		return mediaFolders;
	}
	
    /**
	 * Reads all media folders in baseFolder and returns a list containing
	 * all files therein, ordered by date, oldest files first.
	 * @param baseFolder Biography base folder to search
	 * @return list of media files.
	 */
	public static List<File> getMediaFiles(File baseFolder) {
		List<File> mediaFiles = new ArrayList<>();
		if(baseFolder==null) {
			return mediaFiles;
		}
		if(!baseFolder.exists()) {
			return mediaFiles;
		}
		if(!baseFolder.isDirectory()) {
			return mediaFiles;
		}
		List<File> mediaFolders = getMediaFolders(baseFolder);
		mediaFolders.stream().forEach((mediaFolder) -> {
			File[] files = mediaFolder.listFiles();
			Arrays.stream(files).filter(isMediaFileName()).sorted().forEach((file) -> {
				mediaFiles.add(file);
			});
		}); 
		return mediaFiles;
	}
	
	public static boolean isMediaFileName(File file) {
		return isMediaFileName().test(file);
	}
	
	public static String sha1(File file) {
		if(file==null) {
			return null;
		}
		
		if(!file.exists() || file.isDirectory()) {
			return null;
		}

    	byte[] allBytes;
		try {
			allBytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			return null;
		}
    	return DigestUtils.sha1Hex(allBytes);
	}

	private static Predicate<File> isMediaFileName() {
		return (file) -> {
			if(file==null) {
				return false;
			}
			return FILE_FORMAT.matcher(file.getName()).matches();
		};
	}

	private static Predicate<File> isDirectory() {
		return (file) -> {
			return file.isDirectory();
		};
	}
	
	private static Predicate<File> isFilenameYear() {
		return (file) -> {
			if(!StringUtils.isNumeric(file.getName())) {
				return false;
			}
			int number = Integer.parseInt(file.getName());
			return (1000 <= number) && (number <= 9999);
		};
	}

	private static Predicate<File> isFilenameMonth() {
		return (file) -> {
			return IntStream.range(1, 13).mapToObj(number -> {
				return String.format("%02d", number);
			}).anyMatch(month -> month.equals(file.getName()));
		};
	}
	
}

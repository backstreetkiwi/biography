package de.zaunkoenigweg.biography.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

/**
 * Collection of utils for working with files.
 * 
 * @author Nikolaus Winter
 *
 */
public class BiographyFileUtils {

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

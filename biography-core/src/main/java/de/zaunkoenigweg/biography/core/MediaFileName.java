package de.zaunkoenigweg.biography.core;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Value object for a Biography media file
 */
public class MediaFileName {

	private static Pattern ARCHIVE_FILENAME_FORMAT;

	static {
		String fileExtensions = MediaFileType.all().map(MediaFileType::getFileExtension)
				.collect(Collectors.joining("|"));
		String regex = String.format(
				"^((\\d{4})-(\\d{2})-(\\d{2})--(\\d{2})-(\\d{2})-(\\d{2}))---(\\p{XDigit}{40}).(%s)$", fileExtensions);
		ARCHIVE_FILENAME_FORMAT = Pattern.compile(regex);
	}

	/**
	 * the filename
	 */
	private String filename;

	private MediaFileName(String filename) {
		this.filename = filename;
	}

	/**
	 * Checks if the given filename is valid
	 * 
	 * @param filename name of Biography media file
	 * @return Is the given filename valid?
	 */
	public static boolean isValid(String filename) {
		if (filename == null) {
			return false;
		}

		Matcher matcher = ARCHIVE_FILENAME_FORMAT.matcher(filename);

		if (!matcher.matches()) {
			return false;
		}

		try {
			LocalDateTime.of(Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)),
					Integer.valueOf(matcher.group(4)), Integer.valueOf(matcher.group(5)),
					Integer.valueOf(matcher.group(6)), Integer.valueOf(matcher.group(7)));
		} catch (DateTimeException e) {
			return false;
		}

		return true;
	}

	/**
	 * Creates a Biography media file name value object.
	 * 
	 * @param filename name of the Biography media file
	 * @return Biography media file value object
	 */
	public static MediaFileName of(String filename) {
		if (!isValid(filename)) {
			throw new IllegalArgumentException(
					String.format("'%s' is not a valid Biography media file format.", filename));
		}
		return new MediaFileName(filename);
	}

	public String getFilename() {
		return filename;
	}
}

package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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

	private String filename;
	private MediaFileType type;
	private LocalDateTime dateTimeOriginal;
	private Sha1 sha1;
	
	private MediaFileName(String filename) {
		Matcher matcher = ARCHIVE_FILENAME_FORMAT.matcher(filename);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(String.format("Invalid media file name '%s'", filename));
		}
		this.filename = filename;
		this.type = MediaFileType.of(filename).get();
		this.dateTimeOriginal = LocalDateTime.of(Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)),
				Integer.valueOf(matcher.group(4)), Integer.valueOf(matcher.group(5)),
				Integer.valueOf(matcher.group(6)), Integer.valueOf(matcher.group(7)));
		this.sha1 = Sha1.of(matcher.group(8));
	}

	private MediaFileName(MediaFileType type, LocalDateTime dateTimeOriginal, Sha1 sha1) {
		this.type = Objects.requireNonNull(type);
		this.dateTimeOriginal = Objects.requireNonNull(dateTimeOriginal);
		this.sha1 = Objects.requireNonNull(sha1);
		this.filename = String.format("%s---%s.%s", this.dateTimeOriginal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss")), this.sha1.value(), this.type.getFileExtension());
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
			throw new IllegalArgumentException(String.format("'%s' is not a valid Biography media file format.", filename));
		}
		return new MediaFileName(filename);
	}

	public static MediaFileName of(MediaFileType mediaFileType, LocalDateTime dateTimeOriginal, Sha1 sha1) {
		return new MediaFileName(mediaFileType, dateTimeOriginal, sha1);
	}
	
	/**
	 * Determines the fully qualified {@link File} of a Biography media file with this name.
	 * @param archiveBaseFolder base folder of the Biography archive
	 * @return Biography archive file
	 */
	public File archiveFile(File archiveBaseFolder) {
		Objects.requireNonNull(archiveBaseFolder, "The archive base folder must not be null.");
		return new File(archiveBaseFolder, filename.substring(0, 4) + "/" + filename.substring(5,7) + "/" + filename);
	}
	
	/**
	 * Determines the fully qualified {@link File} of a Biography thumbnail file with this name.
	 * @param thumbnailBaseFolder base folder of the Biography thumbnails
	 * @return Biography archive file
	 */
	public File thumbnailFile(File thumbnailBaseFolder) {
		Objects.requireNonNull(thumbnailBaseFolder, "The thumbnail base folder must not be null.");
		String thumbnailFileName = StringUtils.substringBeforeLast(this.filename, ".") + ".jpg";
		return new File(thumbnailBaseFolder, filename.substring(0, 4) + "/" + filename.substring(5,7) + "/" + thumbnailFileName);
	}
	
	public String getFilename() {
		return filename;
	}
	
	public MediaFileType getType() {
		return this.type;
	}
	
	public LocalDateTime getDateTimeOriginal() {
		return this.dateTimeOriginal;
	}
	
	public Sha1 getSha1() {
		return this.sha1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaFileName other = (MediaFileName) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		return true;
	}
}

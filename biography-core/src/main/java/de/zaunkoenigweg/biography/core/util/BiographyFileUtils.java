package de.zaunkoenigweg.biography.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import de.zaunkoenigweg.biography.core.MediaFileType;

/**
 * Collection of utils for working with files.
 * 
 * @author Nikolaus Winter
 *
 */
public class BiographyFileUtils {

    private final static Pattern ARCHIVE_FILE_FORMAT = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})--(\\d{2})-(\\d{2})-(\\d{2})---(\\p{XDigit}{40}).(jpg|mov)");

    /**
     * Predicate: Does the name of the given folder represent a valid year?
     */
    private final static Predicate<File> IS_VALID_YEAR_FOLDER = (file) -> {

        if (file == null || !file.isDirectory()) {
            return false;
        }

        if (!StringUtils.isNumeric(file.getName())) {
            return false;
        }

        int number = Integer.parseInt(file.getName());
        return (1000 <= number) && (number <= 9999);
    };

    /**
     * Predicate: Does the name of the given file represent a valid month?
     */
    private final static Predicate<File> IS_VALID_MONTH_FOLDER = (file) -> {

        if (file == null || !file.isDirectory()) {
            return false;
        }

        return IntStream.range(1, 13)
                .mapToObj(number -> String.format("%02d", number))
                .anyMatch(month -> month.equals(file.getName()));
    };

    /**
     * Predicate: Is the name of the given file a valid (archived!) media file
     * name?
     */
    private final static Predicate<File> IS_MEDIA_FILE_NAME = (file) -> {

        if (file == null || file.isDirectory()) {
            return false;
        }

        return ARCHIVE_FILE_FORMAT.matcher(file.getName()).matches();
    };

    public static LocalDateTime getDatetimeOriginalFromArchiveFilename(File file) {
        
        if (file == null || file.isDirectory()) {
            return null;
        }

        Matcher matcher = ARCHIVE_FILE_FORMAT.matcher(file.getName());
        if(!matcher.matches()) {
            return null;
        }
        
        return LocalDateTime.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)), Integer.parseInt(matcher.group(6)));
    }
    
    public static String getSha1FromArchiveFilename(File file) {
        
        if (file == null || file.isDirectory()) {
            return null;
        }

        Matcher matcher = ARCHIVE_FILE_FORMAT.matcher(file.getName());
        if(!matcher.matches()) {
            return null;
        }
        
        return matcher.group(7);
    }
    
    /**
     * Reads all media folders in baseFolder and returns a list containing all
     * media folders ordered by date, oldest folders first.
     * 
     * @param baseFolder
     *            Biography base folder to search
     * @return list of media folders.
     */
    public static List<File> getMediaFolders(File baseFolder) {

        if (baseFolder == null || !baseFolder.exists() || !baseFolder.isDirectory()) {
            return Collections.emptyList();
        }

        return Arrays.stream(baseFolder.listFiles())
                .filter(IS_VALID_YEAR_FOLDER)
                .sorted()
                .map(File::listFiles)
                .flatMap(Arrays::stream)
                .filter(IS_VALID_MONTH_FOLDER)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Reads all media folders in baseFolder and returns a list containing all
     * files therein, ordered by date, oldest files first.
     * 
     * @param baseFolder
     *            Biography base folder to search
     * @return list of media files.
     */
    public static List<File> getMediaFiles(File baseFolder) {

        if (baseFolder == null || !baseFolder.exists() || !baseFolder.isDirectory()) {
            return Collections.emptyList();
        }

        return getMediaFolders(baseFolder).stream()
                .map(File::listFiles)
                .flatMap(Arrays::stream)
                .filter(IS_MEDIA_FILE_NAME)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Has the given file a valid (archived) media file name?
     * 
     * @param file
     *            media file to test
     * @return Has the given file a valid (archived) media file name? (false if
     *         null)
     */
    public static boolean isMediaFileName(File file) {
        return IS_MEDIA_FILE_NAME.test(file);
    }

    /**
     * Calculates the SHA-1 of the content of the media file
     * 
     * @param file
     *            media file
     * @return SHA-1
     */
    public static String sha1(File file) {

        if (file == null) {
            return null;
        }

        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        
        Optional<MediaFileType> fileType = MediaFileType.of(file);
        
        if (!fileType.isPresent()) {
            return null;
        }
        
        byte[] allBytes = null;

        switch (fileType.get()) {
        case JPEG:
            allBytes = FILE_BYTES_OF_BUFFERED_IMAGE.apply(file);
            break;

        case QUICKTIME:
            allBytes = FILE_BYTES_OF_COMPLETE_FILE.apply(file);
            break;

        default:
            return null;
        }

        return DigestUtils.sha1Hex(allBytes);

    }
    
    private static final Function<File, byte[]> FILE_BYTES_OF_COMPLETE_FILE = file -> {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return null;
        }
    };

    private static final Function<File, byte[]> FILE_BYTES_OF_BUFFERED_IMAGE = file -> {
        try {
            BufferedImage image = ImageIO.read(file);
            DataBuffer dataBuffer = image.getRaster().getDataBuffer();
            if(dataBuffer.getNumBanks()!=1) {
                return null;
            }
            if(dataBuffer.getDataType()!=DataBuffer.TYPE_BYTE) {
                return null;
            }
            int size = dataBuffer.getSize();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(size);
            for (int i = 0; i < dataBuffer.getSize(); i++) {
                byteArrayOutputStream.write(dataBuffer.getElem(i));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }

    };

    public static Path buildArchiveFilename(File archiveFolder, File file, LocalDateTime dateTimeOriginal, MediaFileType mediaFileType) {
        String folderInArchive = String.format("%04d/%02d", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue());
        String fileName = String.format("%04d-%02d-%02d--%02d-%02d-%02d---%s.%s", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue(),
                dateTimeOriginal.getDayOfMonth(), dateTimeOriginal.getHour(), dateTimeOriginal.getMinute(), dateTimeOriginal.getSecond(), sha1(file), mediaFileType.getFileExtension());
        return Paths.get(archiveFolder.getPath(), folderInArchive, fileName);
    }
    
    public static Function<MediaFileType, Stream<File>> streamFilesOfMediaFileType(final File folder) {
        return mediaFileType -> Arrays
                .stream(folder.listFiles(pathname -> StringUtils.endsWithIgnoreCase(pathname.getName(), mediaFileType.getFileExtension())));
    }

}

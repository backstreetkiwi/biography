package de.zaunkoenigweg.biography.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Collection of utils for working with files.
 * 
 * @author Nikolaus Winter
 *
 */
public class BiographyFileUtils {

    private final static Pattern FILE_FORMAT = Pattern.compile("\\d{4}-\\d{2}-\\d{2}--\\d{2}-\\d{2}-\\d{2}---\\p{XDigit}{40}.(jpg|mov)");

    /**
     * Predicate: Does the name of the given folder represent a valid year?
     */
    private final static Predicate<File> IS_VALID_YEAR_FOLDER = (file) -> {
        
        if (file==null || !file.isDirectory()) {
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
        
        if (file==null || !file.isDirectory()) {
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
        
        if (file==null || file.isDirectory()) {
            return false;
        }
        
        return FILE_FORMAT.matcher(file.getName()).matches();
    };

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

        byte[] allBytes;
        try {
            allBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return null;
        }
        return DigestUtils.sha1Hex(allBytes);
    }

}

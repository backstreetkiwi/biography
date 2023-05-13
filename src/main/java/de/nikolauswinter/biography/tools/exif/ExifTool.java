package de.nikolauswinter.biography.tools.exif;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ExifTool {

    private static final Pattern KEY_VALUE_LINE_PATTERN = Pattern.compile("^([^:]+)\\s*:\\s(.+)$");
    private static final Pattern EXIF_OUTPUT_FILE_DELIMITER = Pattern.compile("^========.*$");

    private ExifTool() {
    }

    /**
     * Reads EXIF metadata for the given file.
     * 
     * @param file Image file.
     * @return EXIF data
     * @throws ExifMappingException  if the EXIF data could not be found or is
     *                               corrupt.
     * @throws IllegalStateException if the file could not be found or is a
     *                               directory.
     */
    public static ExifData read(File file) {
        Objects.requireNonNull(file);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalStateException(MessageFormat.format("The file {0} does not exist or is a directory.", file.getAbsolutePath()));
        }

        Map<File, ExifData> exifData = readAll(file.getAbsolutePath(), false);
        if (!exifData.containsKey(file)) {
            throw new ExifMappingException(MessageFormat.format("No EXIF data could be read for file {0}.", file.getAbsolutePath()));
        }

        return exifData.get(file);
    }

    /**
     * Reads EXIF metadata for all files in the given path pattern.
     * 
     * @param file Image file.
     * @return EXIF data
     * @throws ExifMappingException  if the EXIF data could not be found or is
     *                               corrupt.
     * @throws IllegalStateException if the file could not be found or is a
     *                               directory.
     */
    public static Map<File, ExifData> read(String pathPattern) {
        Objects.requireNonNull(pathPattern);
        return readAll(pathPattern, true);
    }

    /**
     * Reads EXIF metadata from all mediafiles in the given path (pattern) and
     * returns them as a map.
     * 
     * @param pathPattern  Path pattern, passed to exiftool 'as is'.
     * @param ignoreErrors If <code>true</code>, corrupt or missing EXIF data will
     *                     be ignored.
     * @throws ExifMappingException If a file with corrupt or missing EXIF data is
     *                              hit and the <code>ignoreErrors</code> flag is
     *                              not set.
     * @return Map of Files/ExifData.
     */
    private static Map<File, ExifData> readAll(String pathPattern, boolean ignoreErrors) {
        if (StringUtils.isBlank(pathPattern)) {
            throw new IllegalArgumentException("missing argument 'pathPattern'");
        }

        // Exiftool is fed the following params:
        // - each field we want to read
        // - fileName to give us the filename
        // - directory to give us the directory
        // -r for usage on folders which we want to scan recursively
        String exiftoolParamList = ExifData.EXIF_TOOL_PARAMS.stream()
                .map(param -> "-" + param)
                .collect(Collectors.joining(" ")) + " -fileName -directory -r";

        Pair<Integer, List<String>> exiftoolOutput = callExiftool(exiftoolParamList, pathPattern);

        if (0 != exiftoolOutput.getLeft()) {
            String message = String.format("call of exiftool not successful (%d)", exiftoolOutput.getLeft());
            if (exiftoolOutput.getRight() != null) {
                message += "\n" + exiftoolOutput.getRight().stream().collect(Collectors.joining("\n"));
            }
            throw new IllegalStateException(message);
        }

        List<Map<String, String>> rawExifDataByFile = new ArrayList<>();
        Map<String, String> currentFile = new HashMap<>();

        // Iterate over all lines of the exiftool output
        for (String line : exiftoolOutput.getRight()) {
            Matcher separatorMatcher = EXIF_OUTPUT_FILE_DELIMITER.matcher(line);

            // If the file delimiter in the output is found, we add the current file
            // to the map and prepare for the next one.
            if (separatorMatcher.matches() && !currentFile.isEmpty()) {
                rawExifDataByFile.add(currentFile);
                currentFile = new HashMap<>();

                // Any other line will be added as key/value pair to the current file
            } else {
                Optional<Pair<String, String>> parsedLine = parseOutputLine(line);
                if (parsedLine.isPresent()) {
                    currentFile.put(parsedLine.get().getKey(), parsedLine.get().getValue());
                }
            }
        }

        if (!currentFile.isEmpty()) {
            rawExifDataByFile.add(currentFile);
        }

        Map<File, ExifData> result = new HashMap<>();

        rawExifDataByFile.stream().forEach(rawExifData -> {
            try {
                result.put(new File(new File(rawExifData.get("Directory")), rawExifData.get("File Name")), ExifData.of(rawExifData));
            } catch (ExifMappingException e) {
                if (!ignoreErrors) {
                    throw e;
                }
            }
        });

        return result;
    }

    /**
     * Performs a system call to the Linux <code>exiftool</code>.
     * 
     * @param params Parameters passed to <code>exiftool</code>.
     * @param path   Path passed to <code>exiftool</code>.
     * @return pair: exit code and list of raw output lines.
     */
    private static Pair<Integer, List<String>> callExiftool(String params, String path) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            String command = String.format("exiftool %s %s", params, path);
            builder.command("sh", "-c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream inputStream = process.getInputStream();
            List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            return Pair.of(exitCode, lines);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parses the output line of the Linux <code>exiftool</code>.
     * 
     * @param line output line
     * @return pair: EXIF key, raw EXIF value
     */
    private static Optional<Pair<String, String>> parseOutputLine(String line) {
        Matcher matcher = KEY_VALUE_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(Pair.of(matcher.group(1).trim(), matcher.group(2)));
    }
}

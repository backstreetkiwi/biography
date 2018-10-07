package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class Exiftool {

  private static final Pattern LINE_PATTERN = Pattern.compile("^([^:]+)\\s*:\\s(.+)$");

  /**
   * Reads EXIF metadata from the given mediafile.
   * 
   * @param file
   *          mediafile
   * @param fields
   *          Set of fields to read.
   * @return Map of metadata fields.
   */
  public static Map<Exif, String> read(File file, Set<Exif> fields) {
    if (file == null) {
      throw new IllegalArgumentException("missing argument 'file'");
    }
    if (!file.exists()) {
      throw new IllegalArgumentException("given file does not exist");
    }
    if (fields == null) {
      throw new IllegalArgumentException("missing argument 'fields'");
    }
    if (fields.isEmpty()) {
      throw new IllegalArgumentException("given field set is empty");
    }

    String params = fields.stream()
        .map(Exif::getExiftoolParam)
        .map(param -> "-" + param)
        .collect(Collectors.joining(" "));

    Pair<Integer, List<String>> result = callExiftool(params, file);

    if (0 != result.getLeft()) {
      String message = String.format("call of exiftool not successfull (%d)", result.getLeft());
      if (result.getRight() != null) {
        message += "\n" + result.getRight().stream().collect(Collectors.joining("\n"));
      }
      throw new IllegalStateException(message);
    }

    Map<String, String> rawExifDataMap = result.getRight().stream()
        .map(Exiftool::parseLine)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

    return fields.stream()
        .filter(field -> rawExifDataMap.containsKey(field.getExiftoolOutputKey()))
        .collect(Collectors.toMap(Function.identity(), field -> rawExifDataMap.get(field.getExiftoolOutputKey())));
  }

  /**
   * Reads EXIF metadata from the given mediafile.
   * 
   * @param file
   *          mediafile
   * @param fields
   *          Set of fields to read.
   * @return Map of metadata fields.
   */
  public static void write(File file, Map<Exif, String> values) {
    
    String params = values.entrySet().stream()
        .map(entry -> String.format("-%s=\"%s\"", entry.getKey().getExiftoolParam(), StringUtils.replace(entry.getValue(), "\"", "\\\"")))
        .collect(Collectors.joining(" "));
    
    Pair<Integer, List<String>> result = callExiftool(params, file);

    if (0 != result.getLeft()) {
      String message = String.format("call of exiftool not successfull (%d)", result.getLeft());
      if (result.getRight() != null) {
        message += "\n" + result.getRight().stream().collect(Collectors.joining("\n"));
      }
      throw new IllegalStateException(message);
    }
  }

  private static Pair<Integer, List<String>> callExiftool(String params, File file) {
    try {
      ProcessBuilder builder = new ProcessBuilder();
      String command = String.format("exiftool %s %s", params, file.getAbsolutePath());
      builder.command("sh", "-c", command);
      builder.redirectErrorStream(true);
      Process process = builder.start();
      int exitCode = process.waitFor();
      InputStream inputStream = process.getInputStream();
      List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
      return Pair.of(exitCode, lines);
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Optional<Pair<String, String>> parseLine(String line) {
    Matcher matcher = LINE_PATTERN.matcher(line);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    return Optional.of(Pair.of(matcher.group(1).trim(), matcher.group(2)));
  }
}

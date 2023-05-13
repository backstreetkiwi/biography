package de.nikolauswinter.biography.tools.exif;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

/**
 * EXIF field type.
 * 
 * An EXIF field type contains functions for serialization and deserialization
 * from/to the target type T. (The actual EXIF fields are strings.)
 * 
 * (De)serialization tries to mitigate obstacles like missing fields or
 * <code>null</code> values by setting the default values. However, if a field
 * is violating the format badly and cannot be corrected, an
 * {@link ExifMappingException} is thrown.
 * 
 * This class contains static members representing the available field types.
 */
class ExifFieldType<T> {

    private static final DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    static ExifFieldType<Integer> THREE_DIGIT_INTEGER = new ExifFieldType<>(ExifFieldType::toThreeDigitInteger, ExifFieldType::fromThreeDigitInteger);
    static ExifFieldType<String> STRING = new ExifFieldType<>(ExifFieldType::toString, ExifFieldType::fromString);
    static ExifFieldType<LocalDateTime> DATETIME = new ExifFieldType<>(ExifFieldType::toDateTime, ExifFieldType::fromDateTime);

    /**
     * Function to deserialize field from String.
     */
    private Function<String, T> deserializer;

    /**
     * Function to serialize field to String.
     */
    private Function<T, String> serializer;

    private ExifFieldType(Function<String, T> deserializer, Function<T, String> serializer) {
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    Function<String, T> getDeserializer() {
        return deserializer;
    }

    Function<T, String> getSerializer() {
        return serializer;
    }

    private static String fromString(String in) {
        return StringUtils.trimToEmpty(in);
    }

    private static String toString(String in) {
        return StringUtils.trimToNull(in);
    }

    private static String fromThreeDigitInteger(Integer in) {
        if (in == null) {
            return "";
        }
        if (in < 0 || in > 999) {
            throw new ExifMappingException(String.format("%d must be between 0 and 999.", in));
        }
        return String.format("%03d", in);
    }

    private static Integer toThreeDigitInteger(String in) {
        if (StringUtils.isBlank(in)) {
            return null;
        }
        if (!StringUtils.isNumeric(in)) {
            throw new ExifMappingException(String.format("%s is not a numeric value.", in));
        }
        Integer value = Integer.parseInt(in);
        if (value < 0 || value > 999) {
            throw new ExifMappingException(String.format("%s is not a numeric value between 0 and 999.", in));
        }
        return value;
    }

    private static String fromDateTime(LocalDateTime in) {
        if (in == null) {
            return "";
        }
        return EXIF_DATE_TIME_FORMATTER.format(in);
    }

    private static LocalDateTime toDateTime(String in) {
        if (StringUtils.isBlank(in)) {
            return null;
        }
        try {
            return LocalDateTime.parse(StringUtils.replaceChars(in, '-', ':'), EXIF_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ExifMappingException(String.format("EXIF Datetime '%s' could not be parsed.", in));
        }
    }

}

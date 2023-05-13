package de.nikolauswinter.biography.tools.exif;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * EXIF data.
 */
public class ExifData {

    @ExifMapping(exifField = ExifField.IMAGE_DESCRIPTION)
    private Optional<String> imageDescription = Optional.empty();

    @ExifMapping(exifField = ExifField.DATETIME_ORIGINAL)
    private Optional<LocalDateTime> dateTimeOriginal = Optional.empty();

    @ExifMapping(exifField = ExifField.SUBSEC_TIME_ORIGINAL)
    private Optional<Integer> subsecTimeOriginal = Optional.empty();

    @ExifMapping(exifField = ExifField.CAMERA_MAKE)
    private Optional<String> cameraMake = Optional.empty();

    @ExifMapping(exifField = ExifField.CAMERA_MODEL)
    private Optional<String> cameraModel = Optional.empty();

    @ExifMapping(exifField = ExifField.USER_COMMENT)
    private Optional<String> userComment = Optional.empty();

    /**
     * Mapping of {@link ExifField}s to fields (Java reflection) of this class.
     */
    private final static Map<ExifField, Field> MAPPED_FIELDS = Arrays.asList(ExifData.class.getDeclaredFields()).stream()
            .filter(f -> f.isAnnotationPresent(ExifMapping.class))
            .collect(Collectors.toMap((Field f) -> {
                return ((ExifMapping) f.getAnnotation(ExifMapping.class)).exifField();
            }, Function.identity()));

    /**
     * List of params for Linux <code>exiftool</code>.
     */
    final static List<String> EXIF_TOOL_PARAMS = MAPPED_FIELDS.keySet().stream().map(ExifField::getExiftoolParam).collect(Collectors.toUnmodifiableList());

    /**
     * Reads the EXIF data from a String-based map (raw EXIF data) and produces an
     * {@link ExifData} object.
     * 
     * @param rawExif raw map from exiftool
     * @return Exif Data object
     */
    static ExifData of(Map<String, String> rawExif) {

        ExifData exifData = new ExifData();

        MAPPED_FIELDS.keySet().stream()
                .filter(f -> rawExif.containsKey(f.getExifKey()))
                .forEach(exifField -> {
                    Field field = MAPPED_FIELDS.get(exifField);
                    ExifMapping exifMappingAnnotation = (ExifMapping) (field.getAnnotation(ExifMapping.class));
                    try {
                        Object value = exifMappingAnnotation.exifField().getExifFieldType().getDeserializer().apply(rawExif.get(exifField.getExifKey()));
                        field.set(exifData, Optional.ofNullable(value));
                    } catch (IllegalAccessException | IllegalArgumentException e) {
                        throw new ExifMappingException("EXIF data could not be mapped.", e);
                    }
                });

        return exifData;
    }

    public Optional<String> getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(Optional<String> imageDescription) {
        this.imageDescription = imageDescription;
    }

    public Optional<LocalDateTime> getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public void setDateTimeOriginal(Optional<LocalDateTime> dateTimeOriginal) {
        this.dateTimeOriginal = dateTimeOriginal;
    }

    public Optional<Integer> getSubsecTimeOriginal() {
        return subsecTimeOriginal;
    }

    public void setSubsecTimeOriginal(Optional<Integer> subsecTimeOriginal) {
        this.subsecTimeOriginal = subsecTimeOriginal;
    }

    public Optional<String> getCameraMake() {
        return cameraMake;
    }

    public void setCameraMake(Optional<String> cameraMake) {
        this.cameraMake = cameraMake;
    }

    public Optional<String> getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(Optional<String> cameraModel) {
        this.cameraModel = cameraModel;
    }

    public Optional<String> getUserComment() {
        return userComment;
    }

    public void setUserComment(Optional<String> userComment) {
        this.userComment = userComment;
    }
}

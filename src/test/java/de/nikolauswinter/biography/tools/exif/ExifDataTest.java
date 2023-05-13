package de.nikolauswinter.biography.tools.exif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class ExifDataTest {

    @Test
    public void testReadFromStringMap() {
        HashMap<String, String> rawExif = new HashMap<>();
        rawExif.put("Image Description", "the image description");
        rawExif.put("Date/Time Original", "2003:04:08 19:10:57");
        rawExif.put("Sub Sec Time Original", "123");
        rawExif.put("Make", "Nikon");
        rawExif.put("Camera Model Name", "Nikon D70");
        rawExif.put("User Comment", "the user comment");
        ExifData exifData = ExifData.of(rawExif);
        assertThat(exifData).isNotNull();
        assertThat(exifData.getImageDescription()).isPresent();
        assertThat(exifData.getImageDescription()).contains("the image description");
        assertThat(exifData.getDateTimeOriginal()).isPresent();
        assertThat(exifData.getDateTimeOriginal()).contains(LocalDateTime.of(2003, 4, 8, 19, 10, 57));
        assertThat(exifData.getSubsecTimeOriginal()).isPresent();
        assertThat(exifData.getSubsecTimeOriginal()).contains(Integer.valueOf(123));
        assertThat(exifData.getCameraMake()).isPresent();
        assertThat(exifData.getCameraMake()).contains("Nikon");
        assertThat(exifData.getCameraModel()).isPresent();
        assertThat(exifData.getCameraModel()).contains("Nikon D70");
        assertThat(exifData.getUserComment()).isPresent();
        assertThat(exifData.getUserComment()).contains("the user comment");
    }

    @Test
    public void testReadAlternativeDateFormat() {
        HashMap<String, String> rawExif = new HashMap<>();
        rawExif.put("Date/Time Original", "2003-04-08 19:10:57");
        ExifData exifData = ExifData.of(rawExif);
        assertThat(exifData).isNotNull();
        assertThat(exifData.getDateTimeOriginal()).isPresent();
        assertThat(exifData.getDateTimeOriginal()).contains(LocalDateTime.of(2003, 4, 8, 19, 10, 57));
    }

    @Test
    public void testReadWithCorruptDateTimeOriginal() {
        assertThrows(ExifMappingException.class, () -> {
            HashMap<String, String> rawExif = new HashMap<>();
            rawExif.put("Date/Time Original", "2003-04-33 19:10:57");
            ExifData.of(rawExif);
        });
    }

    @Test
    public void testReadWithCorruptSubsecTimeOriginal() {
        assertThrows(ExifMappingException.class, () -> {
            HashMap<String, String> rawExif = new HashMap<>();
            rawExif.put("Sub Sec Time Original", "1234");
            ExifData.of(rawExif);
        });
    }

    @Test
    public void testReadFromEmptyMap() {
        ExifData exifData = ExifData.of(Collections.emptyMap());
        assertThat(exifData).isNotNull();
        assertThat(exifData.getImageDescription()).isNotPresent();
        assertThat(exifData.getDateTimeOriginal()).isNotPresent();
        assertThat(exifData.getSubsecTimeOriginal()).isNotPresent();
        assertThat(exifData.getCameraMake()).isNotPresent();
        assertThat(exifData.getCameraModel()).isNotPresent();
        assertThat(exifData.getUserComment()).isNotPresent();
    }

}

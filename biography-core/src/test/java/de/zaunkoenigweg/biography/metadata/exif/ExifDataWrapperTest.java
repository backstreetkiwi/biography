package de.zaunkoenigweg.biography.metadata.exif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;

import de.zaunkoenigweg.lexi4j.exiftool.ExifData;

public class ExifDataWrapperTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExifDataMissingDateTimeOriginal() {
        new ExifDataWrapper((LocalDateTime)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExifDataMissingRawExifData() {
        new ExifDataWrapper((ExifData)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExifDataEmptyRawExifData() {
        new ExifDataWrapper(new ExifData());
    }

    @Test
    public void testExifData() {
        ExifData rawExifData = new ExifData();
        rawExifData.setDateTimeOriginal(Optional.of(LocalDateTime.of(2018, 9, 30, 12, 13, 14)));
        rawExifData.setSubsecTimeOriginal(Optional.of(789));
        rawExifData.setImageDescription(Optional.of("Something you see on the photo"));
        rawExifData.setUserComment(Optional.of("some json"));
        ExifDataWrapper exifData = new ExifDataWrapper(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14, 789_000_000), exifData.getDateTimeOriginal());
        assertNotNull(exifData.getDescription());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Something you see on the photo", exifData.getDescription().get());
        assertNotNull(exifData.getUserComment());
        assertTrue(exifData.getUserComment().isPresent());
        assertEquals("some json", exifData.getUserComment().get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateTimeOriginalNull() {
        ExifDataWrapper exifData = new ExifDataWrapper(LocalDateTime.now().minusMonths(234));
        exifData.setDateTimeOriginal(null);
    }    
}

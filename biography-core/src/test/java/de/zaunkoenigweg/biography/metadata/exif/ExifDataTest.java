package de.zaunkoenigweg.biography.metadata.exif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;

public class ExifDataTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExifDataMissingDateTimeOriginal() {
        new ExifData((LocalDateTime)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExifDataMissingRawExifData() {
        new ExifData((de.zaunkoenigweg.lexi4j.exiftool.ExifData)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateExifDataEmptyRawExifData() {
        new ExifData(new de.zaunkoenigweg.lexi4j.exiftool.ExifData());
    }

    @Test
    public void testExifData() {
        de.zaunkoenigweg.lexi4j.exiftool.ExifData rawExifData = new de.zaunkoenigweg.lexi4j.exiftool.ExifData();
        rawExifData.setDateTimeOriginal(Optional.of(LocalDateTime.of(2018, 9, 30, 12, 13, 14)));
        rawExifData.setSubsecTimeOriginal(Optional.of(789));
        rawExifData.setImageDescription(Optional.of("Something you see on the photo"));
        rawExifData.setUserComment(Optional.of("some json"));
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14, 789_000_000), exifData.getDateTimeOriginal());
        assertNotNull(exifData.getDescription());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Something you see on the photo", exifData.getDescription().get());
        assertNotNull(exifData.getUserComment());
        assertTrue(exifData.getUserComment().isPresent());
        assertEquals("some json", exifData.getUserComment().get());
    }

    @Test(expected = NullPointerException.class)
    public void testSetDateTimeOriginalNull() {
        ExifData exifData = new ExifData(LocalDateTime.now().minusMonths(234));
        exifData.setDateTimeOriginal(null);
    }    
}

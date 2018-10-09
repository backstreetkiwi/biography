package de.zaunkoenigweg.biography.metadata.exif;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

public class ExifDataTest {

    @Test(expected = IllegalStateException.class)
    public void testCreateExifDataMissingRawExifData() {
        new ExifData(Maps.newHashMap());
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateExifDataEmptyRawExifData() {
        new ExifData(Maps.newHashMap());
    }

    @Test(expected = IllegalStateException.class)
    public void testExifDataMalformedDatetimeOriginal() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "this is NOT a valid datetime field!");
        new ExifData(rawExifData);
    }

    @Test
    public void testExifDataDatetimeOriginalOnly() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "2018:09:30 12:13:14");
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14), exifData.getDateTimeOriginal());
    }

    @Test
    public void testExifDataDatetimeOriginalWithNonNumericSubseconds() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "2018:09:30 12:13:14");
        rawExifData.put(Exif.SUBSEC_TIME_ORIGINAL, "NaN");
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14), exifData.getDateTimeOriginal());
    }

    @Test
    public void testExifDataDatetimeOriginalWithTooLongSubseconds() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "2018:09:30 12:13:14");
        rawExifData.put(Exif.SUBSEC_TIME_ORIGINAL, "9999");
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14), exifData.getDateTimeOriginal());
    }

    @Test
    public void testExifDataDatetimeOriginalWithNegativeSubseconds() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "2018:09:30 12:13:14");
        rawExifData.put(Exif.SUBSEC_TIME_ORIGINAL, "-12");
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14), exifData.getDateTimeOriginal());
    }

    @Test
    public void testExifDataDatetimeOriginalWithValidSubseconds() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "2018:09:30 12:13:14");
        rawExifData.put(Exif.SUBSEC_TIME_ORIGINAL, "789");
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14, 789_000_000), exifData.getDateTimeOriginal());
        assertNotNull(exifData.getDescription());
        assertFalse(exifData.getDescription().isPresent());
        assertNotNull(exifData.getCameraModel());
        assertFalse(exifData.getCameraModel().isPresent());
        assertNotNull(exifData.getUserComment());
        assertFalse(exifData.getUserComment().isPresent());
    }

    @Test
    public void testExifDataGetDateTimeOriginalAsExifFields() {
        ExifData exifData = new ExifData(LocalDateTime.of(2018, 9, 30, 12, 13, 14, 89_000_000));
        Map<Exif, String> dateTimeOriginalExifFields = exifData.getDateTimeOriginalExifFields();
        assertNotNull(dateTimeOriginalExifFields);
        assertEquals(2, dateTimeOriginalExifFields.size());
        assertTrue(dateTimeOriginalExifFields.containsKey(Exif.DATETIME_ORIGINAL));
        assertEquals("2018:09:30 12:13:14", dateTimeOriginalExifFields.get(Exif.DATETIME_ORIGINAL));
        assertTrue(dateTimeOriginalExifFields.containsKey(Exif.DATETIME_ORIGINAL));
        assertEquals("089", dateTimeOriginalExifFields.get(Exif.SUBSEC_TIME_ORIGINAL));
    }

    @Test
    public void testExifDataGetDateTimeOriginalAsExifFieldsWithoutMilliseconds() {
        ExifData exifData = new ExifData(LocalDateTime.of(2018, 9, 30, 12, 13, 14));
        Map<Exif, String> dateTimeOriginalExifFields = exifData.getDateTimeOriginalExifFields();
        assertNotNull(dateTimeOriginalExifFields);
        assertEquals(2, dateTimeOriginalExifFields.size());
        assertTrue(dateTimeOriginalExifFields.containsKey(Exif.DATETIME_ORIGINAL));
        assertEquals("2018:09:30 12:13:14", dateTimeOriginalExifFields.get(Exif.DATETIME_ORIGINAL));
        assertTrue(dateTimeOriginalExifFields.containsKey(Exif.DATETIME_ORIGINAL));
        assertEquals("000", dateTimeOriginalExifFields.get(Exif.SUBSEC_TIME_ORIGINAL));
    }

    @Test
    public void testExifData() {
        Map<Exif, String> rawExifData = new HashMap<>();
        rawExifData.put(Exif.DATETIME_ORIGINAL, "2018:09:30 12:13:14");
        rawExifData.put(Exif.SUBSEC_TIME_ORIGINAL, "789");
        rawExifData.put(Exif.IMAGE_DESCRIPTION, "Something you see on the photo");
        rawExifData.put(Exif.CAMERA_MODEL, "Fancy Gadget 007");
        rawExifData.put(Exif.USER_COMMENT, "some json");
        ExifData exifData = new ExifData(rawExifData);
        assertNotNull(exifData);
        assertEquals(LocalDateTime.of(2018, 9, 30, 12, 13, 14, 789_000_000), exifData.getDateTimeOriginal());
        assertNotNull(exifData.getDescription());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("Something you see on the photo", exifData.getDescription().get());
        assertNotNull(exifData.getCameraModel());
        assertTrue(exifData.getCameraModel().isPresent());
        assertEquals("Fancy Gadget 007", exifData.getCameraModel().get());
        assertNotNull(exifData.getUserComment());
        assertTrue(exifData.getUserComment().isPresent());
        assertEquals("some json", exifData.getUserComment().get());
    }

    @Test
    public void testSetDateTimeOriginal() {
        ExifData exifData = new ExifData(LocalDateTime.now().minusMonths(234));
        LocalDateTime newDateTimeOriginal = LocalDateTime.now().minusDays(1);
        exifData.setDateTimeOriginal(newDateTimeOriginal);
        assertEquals(newDateTimeOriginal, exifData.getDateTimeOriginal());
    }    
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetDateTimeOriginalNull() {
        ExifData exifData = new ExifData(LocalDateTime.now().minusMonths(234));
        exifData.setDateTimeOriginal(null);
    }    
    
    @Test
    public void testSetDescription() {
        ExifData exifData = new ExifData(LocalDateTime.now().minusMonths(234));
        assertNotNull(exifData.getDescription());
        assertFalse(exifData.getDescription().isPresent());
        exifData.setDescription("newdesc");
        assertNotNull(exifData.getDescription());
        assertTrue(exifData.getDescription().isPresent());
        assertEquals("newdesc", exifData.getDescription().get());
    }    
    
    @Test
    public void testSetDescriptionNull() {
        ExifData exifData = new ExifData(LocalDateTime.now().minusMonths(234));
        assertNotNull(exifData.getDescription());
        assertFalse(exifData.getDescription().isPresent());
        exifData.setDescription(null);
        assertNotNull(exifData.getDescription());
        assertFalse(exifData.getDescription().isPresent());
    }    
    
}

package de.nikolauswinter.biography.tools.exif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class ExifFieldTypeTest {

    @Test
    public void testStringDeserialization() {
        assertThat(ExifFieldType.STRING.getDeserializer().apply("foo")).isEqualTo("foo");
        assertThat(ExifFieldType.STRING.getDeserializer().apply(null)).isNull();
        assertThat(ExifFieldType.STRING.getDeserializer().apply("")).isNull();
        assertThat(ExifFieldType.STRING.getDeserializer().apply("   ")).isNull();
    }

    @Test
    public void testStringSerialization() {
        assertThat(ExifFieldType.STRING.getSerializer().apply("foo")).isEqualTo("foo");
        assertThat(ExifFieldType.STRING.getSerializer().apply(null)).isEqualTo("");
        assertThat(ExifFieldType.STRING.getSerializer().apply("")).isEqualTo("");
        assertThat(ExifFieldType.STRING.getSerializer().apply("   ")).isEqualTo("");
    }
    
    @Test
    public void testThreeDigitIntegerSerializationValueTooLow() {
        assertThrows(ExifMappingException.class, () -> {
            ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(-1);
        });
    }

    @Test
    public void testThreeDigitIntegerSerializationValueTooHigh() {
        assertThrows(ExifMappingException.class, () -> {
            ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(1000);
        });
    }

    @Test
    public void testThreeDigitIntegerSerialization() {
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(null)).isEqualTo("");
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(0)).isEqualTo("000");
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(1)).isEqualTo("001");
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(12)).isEqualTo("012");
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(123)).isEqualTo("123");
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getSerializer().apply(999)).isEqualTo("999");
    }

    @Test
    public void testThreeDigitIntegerDeserializationValueTooLow() {
        assertThrows(ExifMappingException.class, () -> {
            ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("-1");
        });
    }

    @Test
    public void testThreeDigitIntegerDeserializationValueTooHigh() {
        assertThrows(ExifMappingException.class, () -> {
            ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("1000");
        });
    }

    @Test
    public void testThreeDigitIntegerDeserializationNoNumericValue() {
        assertThrows(ExifMappingException.class, () -> {
            ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("something");
        });
    }

    @Test
    public void testThreeDigitIntegerDeserialization() {
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply(null)).isNull();
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("")).isNull();
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("    ")).isNull();
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("0")).isEqualTo(Integer.valueOf(0));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("000")).isEqualTo(Integer.valueOf(0));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("1")).isEqualTo(Integer.valueOf(1));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("001")).isEqualTo(Integer.valueOf(1));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("12")).isEqualTo(Integer.valueOf(12));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("012")).isEqualTo(Integer.valueOf(12));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("123")).isEqualTo(Integer.valueOf(123));
        assertThat(ExifFieldType.THREE_DIGIT_INTEGER.getDeserializer().apply("999")).isEqualTo(Integer.valueOf(999));
    }

    @Test
    public void testDatetimeSerialization() {
        assertThat(ExifFieldType.DATETIME.getSerializer().apply(null)).isEqualTo("");
        assertThat(ExifFieldType.DATETIME.getSerializer().apply(LocalDateTime.of(2018, 12, 24, 21, 55, 53))).isEqualTo("2018:12:24 21:55:53");
    }

    @Test
    public void testDatetimeDeserialization() {
        assertThat(ExifFieldType.DATETIME.getDeserializer().apply(null)).isNull();
        assertThat(ExifFieldType.DATETIME.getDeserializer().apply("")).isNull();
        assertThat(ExifFieldType.DATETIME.getDeserializer().apply("   ")).isNull();
    }

    @Test
    public void testDatetimeDeserializationInvalidValue() {
        assertThrows(ExifMappingException.class, () -> {
            ExifFieldType.DATETIME.getDeserializer().apply("something");
        });
    }
    
}

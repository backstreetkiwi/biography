package de.zaunkoenigweg.biography.core.index;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public interface Index {

    String FIELD_ID = "fileName";
    String FIELD_DESCRIPTION = "description";
    String FIELD_ALBUMS = "albums";
    String FIELD_DATETIME_ORIGINAL = "dateTimeOriginal";
    String FIELD_YEAR_LONG_POINT = "yearLongPoint";
    String FIELD_YEAR_MONTH_LONG_POINT = "yearMonthLongPoint";
    String FIELD_DATE_LONG_POINT = "dateLongPoint";

    public static Long toLongPoint(LocalDateTime localDateTime) {
    		return Long.valueOf(localDateTime.getYear() * 10000 + localDateTime.getMonthValue() * 100 + localDateTime.getDayOfMonth());
    }

    public static YearMonth yearMonthFromLongPoint(String longPoint) {
        return YearMonth.parse(longPoint, DateTimeFormatter.ofPattern("yyyyMM"));
    }
    
    public static String queryString(String fieldName, String value) {
        return String.format("%s:\"%s\"", fieldName, value);
    }

}

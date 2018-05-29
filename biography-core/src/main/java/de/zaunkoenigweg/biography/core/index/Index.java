package de.zaunkoenigweg.biography.core.index;

import java.time.LocalDate;
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

    public static final DateTimeFormatter YEAR_MONTH_LONG_POINT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    
    public static final DateTimeFormatter LOCAL_DATE_LONG_POINT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public static String localDateToLongPoint(LocalDate localDate) {
    		return LOCAL_DATE_LONG_POINT_FORMATTER.format(localDate);
    }

    public static LocalDate longPointToLocalDate(String longPoint) {
        return LocalDate.parse(longPoint, LOCAL_DATE_LONG_POINT_FORMATTER);
    }
    
    public static String yearMonthToLongPoint(YearMonth yearMonth) {
        return YEAR_MONTH_LONG_POINT_FORMATTER.format(yearMonth);
    }
    
    public static YearMonth longPointToYearMonth(String longPoint) {
        return YearMonth.parse(longPoint, YEAR_MONTH_LONG_POINT_FORMATTER);
    }
    
    public static String queryString(String fieldName, String value) {
        return String.format("%s:\"%s\"", fieldName, value);
    }

}

package de.zaunkoenigweg.biography.core.index;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Constants and helpers for Index stuff.
 */
class Index {

    static final String FIELD_ID = "fileName";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_ALBUMS = "albums";
    static final String FIELD_DATETIME_ORIGINAL = "dateTimeOriginal";
    static final String FIELD_YEAR_LONG_POINT = "yearLongPoint";
    static final String FIELD_YEAR_MONTH_LONG_POINT = "yearMonthLongPoint";
    static final String FIELD_DATE_LONG_POINT = "dateLongPoint";
    static final String FIELD_DATETIME_LONG_POINT = "dateTimeLongPoint";

    static final DateTimeFormatter YEAR_MONTH_LONG_POINT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    static final DateTimeFormatter LOCAL_DATE_LONG_POINT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    static final DateTimeFormatter LOCAL_DATETIME_LONG_POINT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    // all static
    private Index() {
    }

	static String localDateToLongPoint(LocalDate localDate) {
		return LOCAL_DATE_LONG_POINT_FORMATTER.format(localDate);
    }

    static String localDateTimeToLongPoint(LocalDateTime localDateTime) {
		return LOCAL_DATETIME_LONG_POINT_FORMATTER.format(localDateTime);
    }

    static LocalDate longPointToLocalDate(String longPoint) {
        return LocalDate.parse(longPoint, LOCAL_DATE_LONG_POINT_FORMATTER);
    }
    
    static String yearMonthToLongPoint(YearMonth yearMonth) {
        return YEAR_MONTH_LONG_POINT_FORMATTER.format(yearMonth);
    }
    
    static YearMonth longPointToYearMonth(String longPoint) {
        return YearMonth.parse(longPoint, YEAR_MONTH_LONG_POINT_FORMATTER);
    }
    
    static String queryString(String fieldName, String value) {
        return String.format("%s:\"%s\"", fieldName, value);
    }

}

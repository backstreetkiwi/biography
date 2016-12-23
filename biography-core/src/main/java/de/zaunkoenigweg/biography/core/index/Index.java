package de.zaunkoenigweg.biography.core.index;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public interface Index {

    String KEY_FIELD_NAME = "name";
    String KEY_FIELD_MULTI_VALUED = "multiValued";
    String KEY_FIELD_TYPE = "type";

    String FIELD_TYPE_LONG_POINT = "long";
    String FIELD_TYPE_TEXT = "text_general";
    String FIELD_TYPE_STRING = "string";

    String FIELD_ID = "fieldName";
    String FIELD_DESCRIPTION = "description";
    String FIELD_ALBUM_TITLES = "albumTitles";
    String FIELD_ALBUM_CHAPTERS = "albumChapters";
    String FIELD_DATE_TIME_ORIGINAL = "dateTimeOriginal";
    String FIELD_DATE_ORIGINAL_LONG_POINT = "dateOriginal";
    String FIELD_YEAR = "year";
    String FIELD_MONTH = "month";
    String FIELD_DAY = "day";

    Function<Map<String, Object>, String> TO_FIELD_NAME = fieldAttributes -> fieldAttributes.get(KEY_FIELD_NAME).toString();

    ToLongFunction<LocalDateTime> DATETIME_TO_LONG_POINT = dateTime -> dateTime.getYear() * 10000 + dateTime.getMonthValue() * 100 + dateTime.getDayOfMonth();

    static Stream<Map<String, Object>> fields() {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(fieldAttributes(FIELD_ID, FIELD_TYPE_STRING, false));
        fields.add(fieldAttributes(FIELD_DESCRIPTION, FIELD_TYPE_TEXT, false));
        fields.add(fieldAttributes(FIELD_ALBUM_TITLES, FIELD_TYPE_STRING, true));
        fields.add(fieldAttributes(FIELD_ALBUM_CHAPTERS, FIELD_TYPE_STRING, true));
        fields.add(fieldAttributes(FIELD_DATE_TIME_ORIGINAL, FIELD_TYPE_STRING, false));
        fields.add(fieldAttributes(FIELD_DATE_ORIGINAL_LONG_POINT, FIELD_TYPE_LONG_POINT, false));
        fields.add(fieldAttributes(FIELD_YEAR, FIELD_TYPE_LONG_POINT, false));
        fields.add(fieldAttributes(FIELD_MONTH, FIELD_TYPE_LONG_POINT, false));
        fields.add(fieldAttributes(FIELD_DAY, FIELD_TYPE_LONG_POINT, false));
        return fields.stream();
    }

    static Map<String, Object> fieldAttributes(String fieldName, String type, boolean multiValued) {
        Map<String, Object> fieldAttributes = new HashMap<>();
        fieldAttributes.put(KEY_FIELD_NAME, fieldName);
        fieldAttributes.put(KEY_FIELD_TYPE, type);
        fieldAttributes.put(KEY_FIELD_MULTI_VALUED, multiValued);
        return fieldAttributes;
    }
    
    static String queryString(String fieldName, String value) {
        return String.format("%s:\"%s\"", fieldName, value);
    }

}

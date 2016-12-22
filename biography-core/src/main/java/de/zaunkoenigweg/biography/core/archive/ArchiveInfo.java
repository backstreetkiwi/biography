package de.zaunkoenigweg.biography.core.archive;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ArchiveInfo {

    private Map<YearMonth, Integer> countPerYearMonth = new HashMap<>();

    public int getCount() {
        return countPerYearMonth.entrySet()
                                .stream()
                                .map(Entry::getValue)
                                .collect(Collectors.summingInt(Integer::intValue));
    }

    public int getCount(Integer year) {
        return countPerYearMonth.entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getYear() == year)
                                .map(Entry::getValue)
                                .collect(Collectors.summingInt(Integer::intValue));
    }

    public int getCount(YearMonth yearMonth) {
        return countPerYearMonth.containsKey(yearMonth) ? countPerYearMonth.get(yearMonth) : 0;
    }

    public void setCount(YearMonth yearMonth, Integer count) {
        countPerYearMonth.put(yearMonth, count);
    }
}

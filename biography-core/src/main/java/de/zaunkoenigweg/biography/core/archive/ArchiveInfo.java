package de.zaunkoenigweg.biography.core.archive;

import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArchiveInfo {

    private Map<YearMonth, Long> countPerYearMonth = new HashMap<>();

    private Map<String, AlbumInfo> albums = new HashMap<>();

    public long getCount() {
        return countPerYearMonth.entrySet()
                                .stream()
                                .map(Entry::getValue)
                                .collect(Collectors.summingLong(Long::longValue));
    }

    public long getCount(Year year) {
        return countPerYearMonth.entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getYear() == year.getValue())
                                .map(Entry::getValue)
                                .collect(Collectors.summingLong(Long::longValue));
    }

    public long getCount(YearMonth yearMonth) {
        return countPerYearMonth.containsKey(yearMonth) ? countPerYearMonth.get(yearMonth) : 0;
    }

    public void setCount(YearMonth yearMonth, Long count) {
        countPerYearMonth.put(yearMonth, count);
    }

    public Stream<Year> years() {
        return countPerYearMonth.keySet()
                                .stream()
                                .map(YearMonth::getYear)
                                .map(Year::of)
                                .distinct()
                                .sorted();
    }

    public Stream<YearMonth> months() {
        return countPerYearMonth.keySet()
                                .stream()
                                .sorted();
    }

    public Stream<YearMonth> months(Year year) {
        return countPerYearMonth.keySet()
                                .stream()
                                .filter(month -> month.getYear() == year.getValue())
                                .sorted();
    }

    public void add(AlbumInfo album) {
        this.albums.put(album.getName(), album);
    }

    public Stream<AlbumInfo> albums() {
        return this.albums.values().stream().sorted();
    }
}

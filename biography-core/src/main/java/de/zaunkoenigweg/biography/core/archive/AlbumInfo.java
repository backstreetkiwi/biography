package de.zaunkoenigweg.biography.core.archive;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlbumInfo extends AbstractAlbumInfo {

    private Map<String, ChapterInfo> chapters = new HashMap<>();

    public AlbumInfo(String name) {
        super(name);
    }

    @Override
    public long getCount() {
        return chapters.entrySet()
                       .stream()
                       .map(Entry::getValue)
                       .map(ChapterInfo::getCount)
                       .collect(Collectors.summingLong(Long::longValue));
    }

    public void add(ChapterInfo chapter) {
        this.chapters.put(chapter.getName(), chapter);
    }

    public Stream<ChapterInfo> chapters() {
        return this.chapters.values().stream().sorted();
    }

    @Override
    public LocalDate getStartDate() {
        return chapters.values()
                .stream()
                .map(ChapterInfo::getStartDate)
                .filter(date -> date!=null)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public LocalDate getEndDate() {
        return chapters.values()
                .stream()
                .map(ChapterInfo::getStartDate)
                .filter(date -> date!=null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}

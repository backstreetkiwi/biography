package de.zaunkoenigweg.biography.core.archive;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AlbumInfo {

    public static final Comparator<AlbumInfo> COMPARE_BY_START_DATE = Comparator.comparing(AlbumInfo::getStartDate); 

    private String name;
    private long count;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private List<AlbumInfo> chapters = new ArrayList<>();
    
    public AlbumInfo(String name) {
        super();
        this.name = name;
    }

    public AlbumInfo(String name, long count) {
        super();
        this.name = name;
        this.count = count;
    }

    @Override
    public String toString() {
        BiFunction<LocalDate,LocalDate,String> startEndToString = (startDate, endDate) -> {
            if(startDate.equals(endDate)) {
                return String.format("(%s)", startDate);
            } else {
                return String.format("(%s to %s)", startDate, endDate);
            }
        };
        StringBuilder string = new StringBuilder(String.format("Album '%s' contains %d media files %s", getName(), getCount(), startEndToString.apply(getStartDate(), getEndDate()))); 
        if(!chapters.isEmpty()) {
            string.append(String.format("  (%d chapters)", chapters.size()));
            string.append(chapters.stream().map(AlbumInfo::toString).collect(Collectors.joining("\n  chapter: ", "\n  chapter: ", "\n")));
        }
        return string.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<AlbumInfo> getChapters() {
        return chapters;
    }
}

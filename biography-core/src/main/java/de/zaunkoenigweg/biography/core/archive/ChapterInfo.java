package de.zaunkoenigweg.biography.core.archive;

import java.time.LocalDate;

public class ChapterInfo extends AbstractAlbumInfo {
    
    private long count;
    private LocalDate startDate;
    private LocalDate endDate;

    public ChapterInfo(String name, long count) {
        super(name);
        this.count = count;
    }

    @Override
    public long getCount() {
        return this.count;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    
}

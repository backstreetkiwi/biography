package de.zaunkoenigweg.biography.core.archive;

import java.time.LocalDate;

public abstract class AbstractAlbumInfo implements Comparable<AbstractAlbumInfo> {

    private String name;

    AbstractAlbumInfo(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract long getCount();

    public abstract LocalDate getStartDate();

    public abstract LocalDate getEndDate();

    @Override
    public int compareTo(AbstractAlbumInfo otherAlbum) {
        if (this.getStartDate() == null) {
            return -1;
        }
        return this.getStartDate().compareTo(otherAlbum.getStartDate());
    }

    @Override
    public String toString() {
        String date = "";
        if(this.getStartDate()!=null && this.getEndDate()!=null) {
            if(this.getStartDate().equals(this.getEndDate())) {
                date = String.format("[%s] ", this.getStartDate());
            } else {
                date = String.format("[%s - %s] ", this.getStartDate(), this.getEndDate());
            }
        }
        return String.format("'%s' %s(%d)", name, date, getCount());
    }
}
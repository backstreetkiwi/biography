package de.zaunkoenigweg.biography.core.index;

import java.time.LocalDate;

public class Album {

    private String name;
    private long size;
    private LocalDate begin;
    private LocalDate end;
    
    public Album(String name, long size, LocalDate begin, LocalDate end) {
        super();
        this.name = name;
        this.size = size;
        this.begin = begin;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public LocalDate getEnd() {
        return end;
    }

}

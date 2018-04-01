package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class BulkImportJob {
    
    private boolean running;
    private SortedSet<File> files = new TreeSet<>();
    private Map<File, MediaFileType> mediaFileType = new HashMap<>();
    private Map<File, ExifData> exifData = new HashMap<>();
    private Map<File, LocalDateTime> dateTimeOriginal = new HashMap<>();
    private Set<File> readyToImport = new HashSet<>();
    private Map<File, ImportResult> importResult = new HashMap<>();
    
    public List<File> getImportFiles() {
        return this.files.stream().collect(Collectors.toList());
    }
    
    public void add(File file) {
        files.add(file);
    }
    
    public MediaFileType getMediaFileType(File file) {
        return mediaFileType.get(file);
    }
    
    public void setMediaFileType(File file, MediaFileType mediaFileType) {
        this.mediaFileType.put(file, mediaFileType);
    }
    
    public ExifData getExifData(File file) {
        return exifData.get(file);
    }
    
    public void setExifData(File file, ExifData exifData) {
        this.exifData.put(file, exifData);
    }

    public LocalDateTime getDateTimeOriginal(File file) {
        return dateTimeOriginal.get(file);
    }
    
    public void setDateTimeOriginal(File file, LocalDateTime dateTimeOriginal) {
        this.dateTimeOriginal.put(file, dateTimeOriginal);
    }
    
    public ImportResult getImportResult(File file) {
        return importResult.get(file);
    }
    
    public void setImportResult(File file, ImportResult importResult) {
        this.importResult.put(file, importResult);
    }
    
    public boolean isReadyToImport(File file) {
        return this.readyToImport.contains(file);
    }
    
    public void setReadyToImport(File file, boolean readyToImport) {
        if(readyToImport) {
            this.readyToImport.add(file);
        } else {
            this.readyToImport.remove(file);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    
    
}

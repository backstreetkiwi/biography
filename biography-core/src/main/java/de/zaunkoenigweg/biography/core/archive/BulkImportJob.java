package de.zaunkoenigweg.biography.core.archive;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataWrapper;

/**
 * Holds the data of a bulk import job
 * 
 * @author mail@nikolaus-winter.de
 */
public class BulkImportJob {
    
    private boolean running;
    private SortedSet<File> files = new TreeSet<>();
    private Map<File, MediaFileType> mediaFileType = new HashMap<>();
    private Map<File, ExifDataWrapper> exifData = new HashMap<>();
    private Map<File, LocalDateTime> dateTimeOriginal = new HashMap<>();
    private Map<File, ImportResult> importResult = new HashMap<>();
    private Map<File, String> albums = new HashMap<>();
    private Map<File, String> descriptions = new HashMap<>();
    
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
    
    public ExifDataWrapper getExifData(File file) {
        return exifData.get(file);
    }
    
    public void setExifData(File file, ExifDataWrapper exifData) {
        this.exifData.put(file, exifData);
    }

    public LocalDateTime getDateTimeOriginal(File file) {
        return dateTimeOriginal.get(file);
    }
    
    public boolean hasDateTimeOriginal(File file) {
        return dateTimeOriginal.containsKey(file);
    }
    
    public void setDateTimeOriginal(File file, LocalDateTime dateTimeOriginal) {
        this.dateTimeOriginal.put(file, dateTimeOriginal);
    }
    
    public String getAlbum(File file) {
        return albums.get(file);
    }
    
    public void setAlbum(File file, String album) {
        this.albums.put(file, album);
    }
    
    public String getDescription(File file) {
        return descriptions.get(file);
    }
    
    public void setDescription(File file, String description) {
        this.descriptions.put(file, description);
    }
    
    public ImportResult getImportResult(File file) {
        return importResult.get(file);
    }
    
    public void setImportResult(File file, ImportResult importResult) {
        this.importResult.put(file, importResult);
    }
    
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
    
    
}

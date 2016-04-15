package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Importer {

    private static final Logger LOG = LogManager.getLogger(Importer.class);

    private File importFolder;
    private File archive;

    private final static DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");;
    
    private boolean isInitialized() {
        if(this.importFolder==null) {
            LOG.trace("importFolder is null");
            return false;
        }
        if(this.archive==null) {
            LOG.trace("archive folder is null");
            return false;
        }
        if(!(this.importFolder.exists() && this.importFolder.isDirectory())) {
            LOG.trace(String.format("importFolder does not exist or is not a folder: %s", importFolder.getAbsolutePath()));
            return false;
        }
        if(!(this.archive.exists() && this.archive.isDirectory())) {
            LOG.trace(String.format("archive folder does not exist or is not a folder: %s", archive.getAbsolutePath()));
            return false;
        }
        if(this.archive.equals(this.importFolder)) {
            LOG.trace("importFolder must not be the same as archive folder");
            return false;
        }
        return true;
    }
    
    public void importAll() {
        if(!isInitialized()) {
            throw new IllegalStateException("Importer not correctly initialized.");
        }
        File[] imageFiles = importFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if(name.endsWith(".jpg")) {
                    return true;
                }
                if(name.endsWith(".JPG")) {
                    return true;
                }
                return false;
            }
        });
        File imageFile = null;
        for (int i = 0; i < imageFiles.length; i++) {
            imageFile = imageFiles[i];
            LOG.trace(String.format("Importing file %s", imageFile.getAbsolutePath()));
            importJpeg(imageFile);
        }
        
    }
    
    public File getImportFolder() {
        return importFolder;
    }
    public void setImportFolder(File importFolder) {
        this.importFolder = importFolder;
    }
    public File getArchive() {
        return archive;
    }
    public void setArchive(File archive) {
        this.archive = archive;
    }
    
    private void importJpeg(File imageFile) {
        
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(imageFile);
        } catch (ImageProcessingException | IOException e) {
            LOG.error(String.format("Error reading EXIF data of %s", imageFile.getAbsolutePath()));
            LOG.error(e);
            throw new IllegalStateException(String.format("Error reading EXIF data of %s", imageFile.getAbsolutePath()));
        }
        
        LocalDateTime dateTimeOriginal = getDateTimeOriginal(metadata);
        LOG.trace(String.format("Image file '%s' has Date/Time original: %s", imageFile.getName(), dateTimeOriginal));
        
        String relativeFolderInArchive = String.format("%04d/%02d", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue());
        String fileName = String.format("%04d-%02d-%02d--%02d-%02d-%02d.jpg", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue(), dateTimeOriginal.getDayOfMonth(), dateTimeOriginal.getHour(), dateTimeOriginal.getMinute(), dateTimeOriginal.getSecond());
        Path imagePathInArchive = Paths.get(this.archive.getPath(), relativeFolderInArchive, fileName);
        LOG.trace(imagePathInArchive);
        try {
            Files.createDirectories(imagePathInArchive.getParent());
            Files.copy(Paths.get(imageFile.toURI()), imagePathInArchive);
        } catch (IOException e) {
            LOG.error(e);
        }
    }
    
    private LocalDateTime getDateTimeOriginal(Metadata metadata) {
        LocalDateTime dateTimeOriginal = null;
        if(metadata==null) {
            LOG.trace("metadata is null");
            return null;
        }
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if(exifSubIFDDirectory==null) {
            LOG.trace("exifSubIFDDirectory is null");
            return null;
        }
        Object object = exifSubIFDDirectory.getObject(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        if(object==null) {
            LOG.trace("exifSubIFDDirectory has no TAG_DATETIME_ORIGINAL");
            return null;
        }
        if(!(object instanceof String)) {
            LOG.trace("TAG_DATETIME_ORIGINAL does not contain a string");
            return null;
        }
        try {
            dateTimeOriginal = LocalDateTime.parse((String)object, EXIF_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            LOG.trace(String.format("Date '%s' could not be parsed.", object));
        }
        return dateTimeOriginal;
    }
}





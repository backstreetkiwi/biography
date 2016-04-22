package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

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

    private final static DateTimeFormatter EXIF_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss:SSS");;

    private boolean isInitialized() {
        if (this.importFolder == null) {
            LOG.trace("importFolder is null");
            return false;
        }
        if (this.archive == null) {
            LOG.trace("archive folder is null");
            return false;
        }
        if (!(this.importFolder.exists() && this.importFolder.isDirectory())) {
            LOG.trace(String.format("importFolder does not exist or is not a folder: %s", importFolder.getAbsolutePath()));
            return false;
        }
        if (!(this.archive.exists() && this.archive.isDirectory())) {
            LOG.trace(String.format("archive folder does not exist or is not a folder: %s", archive.getAbsolutePath()));
            return false;
        }
        if (this.archive.equals(this.importFolder)) {
            LOG.trace("importFolder must not be the same as archive folder");
            return false;
        }
        return true;
    }

    /**
     * imports all images from import folder into archive
     */
    public void importAll() {
        if (!isInitialized()) {
            throw new IllegalStateException("Importer not correctly initialized.");
        }

        Map<Path, File> importMap = this.buildImportMap();
        LOG.info(String.format("Found %d images to import in %s", importMap.size(), this.importFolder.getAbsolutePath()));

        boolean anyImageExistsinArchive = importMap.keySet().stream().anyMatch(path -> {
            if (Files.exists(path)) {
                LOG.error(String.format("Path %s already exists in archive.", path));
                return true;
            }
            return false;
        });

        if (anyImageExistsinArchive) {
            throw new RuntimeException("Import cancelled. At least one image seems to exist in archive.");
        }

        importMap.keySet().stream().forEach(path -> {
            try {
                Files.createDirectories(path.getParent());
                Files.copy(Paths.get(importMap.get(path).toURI()), path);
                LOG.trace(String.format("Copied file %s to %s.", importMap.get(path).getAbsolutePath(), path));
            } catch (Exception e) {
                LOG.error(e);
                throw new RuntimeException(String.format("Error copying file %s to %s.", importMap.get(path), path));
            }
        });

        LOG.info(String.format("Copied %d images into archive.", importMap.size()));
    }

    /**
     * builds map of images to copy.
     * 
     * @return Map (filename in archive -> file in import folder)
     */
    private Map<Path, File> buildImportMap() {
        Map<Path, File> importMap = new HashMap<>();

        File[] imageFiles = importFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (name.endsWith(".jpg")) {
                    return true;
                }
                if (name.endsWith(".JPG")) {
                    return true;
                }
                return false;
            }
        });
        File imageFile = null;
        Path archiveFilename;
        for (int i = 0; i < imageFiles.length; i++) {
            imageFile = imageFiles[i];
            archiveFilename = getArchiveFilename(imageFile);
            if (importMap.containsKey(archiveFilename)) {
                LOG.error(String.format("2 files seem to contain the same image: #1: %s, #2: %s.", imageFile.getName(), importMap.get(archiveFilename).getName()));
                throw new RuntimeException("Import cancelled. At least one pair of image files in the import folder seem to contain the same image.");
            }
            importMap.put(archiveFilename, imageFile);
        }

        return importMap;
    }

    private Path getArchiveFilename(File imageFile) {

        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(imageFile);
        } catch (ImageProcessingException | IOException e) {
            LOG.error(String.format("Error reading EXIF data of %s", imageFile.getAbsolutePath()));
            LOG.error(e);
            throw new IllegalStateException(String.format("Error reading EXIF data of %s", imageFile.getAbsolutePath()));
        }

        LocalDateTime dateTimeOriginal = getDateTimeOriginal(metadata, imageFile.getName());
        String relativeFolderInArchive = String.format("%04d/%02d", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue());
        String fileName = String.format("%04d-%02d-%02d--%02d-%02d-%02d-%03d.jpg", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue(), dateTimeOriginal.getDayOfMonth(),
                dateTimeOriginal.getHour(), dateTimeOriginal.getMinute(), dateTimeOriginal.getSecond(), (dateTimeOriginal.getNano() / 1000000));
        Path imagePathInArchive = Paths.get(this.archive.getPath(), relativeFolderInArchive, fileName);
        LOG.trace(String.format("The archive filename of '%s' will be %s", imageFile, imagePathInArchive));
        return imagePathInArchive;
    }

    private LocalDateTime getDateTimeOriginal(Metadata metadata, String filename) {
        LocalDateTime dateTimeOriginal = null;
        if (metadata == null) {
            LOG.trace(String.format("File %s: metadata is null", filename));
            return null;
        }
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifSubIFDDirectory == null) {
            LOG.trace(String.format("File %s: exifSubIFDDirectory is null", filename));
            LOG.trace("exifSubIFDDirectory is null");
            return null;
        }
        Object objectDateTimeOriginal = exifSubIFDDirectory.getObject(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        if (objectDateTimeOriginal == null) {
            LOG.trace(String.format("File %s: exifSubIFDDirectory has no TAG_DATETIME_ORIGINAL", filename));
            return null;
        }
        if (!(objectDateTimeOriginal instanceof String)) {
            LOG.trace(String.format("File %s: TAG_DATETIME_ORIGINAL does not contain a string", filename));
            return null;
        }
        Object objectSubsecondTimeOriginal = exifSubIFDDirectory.getObject(ExifSubIFDDirectory.TAG_SUBSECOND_TIME_ORIGINAL);
        if (!(objectSubsecondTimeOriginal instanceof String)) {
            LOG.trace(String.format("File %s: TAG_SUBSECOND_TIME_ORIGINAL does not contain a string", filename));
            objectSubsecondTimeOriginal = "000";
        }
        String dateString = String.format("%s:%03d", objectDateTimeOriginal, Integer.parseInt((String) objectSubsecondTimeOriginal));
        try {
            dateTimeOriginal = LocalDateTime.parse(dateString, EXIF_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            LOG.trace(String.format("Date '%s' could not be parsed.", objectDateTimeOriginal));
        }
        return dateTimeOriginal;
    }

    private void dumpExif(Metadata metadata) {
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                LOG.trace(String.format("[%s] - %s = %s", directory.getName(), tag.getTagName(), tag.getDescription()));
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    LOG.error(String.format("ERROR: %s", error));
                }
            }
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
}

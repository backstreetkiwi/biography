package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Importer {

    private static final String EXTENSION_JPG = ".jpg";
    private static final String EXTENSION_MOV = ".mov";

	private static final Logger LOG = LogManager.getLogger(Importer.class);

    private File importFolder;
    private File archive;

    private boolean isInitialized() {
    	// TODO: Use BiographyConfig
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

        addFilesToImportMap(EXTENSION_JPG, importMap, dateTimeOriginalFromExif, dateTimeOriginalFromFilesystem);
        addFilesToImportMap(EXTENSION_MOV, importMap, dateTimeOriginalFromFilesystem);
        
        return importMap;
    }

	private void addFilesToImportMap(String extension, Map<Path, File> importMap, Function<File, LocalDateTime>... extractorFuctions) {
        File[] imageFiles = importFolder.listFiles(extensionFileFilter(extension));
        File imageFile = null;
        Path archiveFilename = null;
        for (int i = 0; i < imageFiles.length; i++) {
            imageFile = imageFiles[i];
            Optional<LocalDateTime> dateTimeOriginal = extractDateTimeOriginal(imageFile, extractorFuctions);
            if(!dateTimeOriginal.isPresent()) {
            	LOG.error(String.format("Date/Time Original could not be extracted for %s. File will be skipped.", imageFile.getAbsolutePath()));
            	continue;
            }
            String sha1 = BiographyFileUtils.sha1(imageFile);
            archiveFilename = buildArchiveFilename(dateTimeOriginal.get(), sha1, extension);
            if (importMap.containsKey(archiveFilename)) {
                LOG.error(String.format("2 files seem to contain the same image: #1: %s, #2: %s.", imageFile.getName(), importMap.get(archiveFilename).getName()));
            }
            importMap.put(archiveFilename, imageFile);
        }
    }
    
	private FileFilter extensionFileFilter(final String extension) {
		return (FileFilter) pathname -> {
			return StringUtils.endsWithIgnoreCase(pathname.getName(), extension);
		};
	}

	private Function<File, LocalDateTime> dateTimeOriginalFromExif = (imageFile) -> {
		ExifData exifData = ExifData.from(imageFile);
		return exifData.getDateTimeOriginal();
	};
	
	private Function<File, LocalDateTime> dateTimeOriginalFromFilesystem = (imageFile) -> {
		return LocalDateTime.ofEpochSecond(imageFile.lastModified()/1000, 0, ZoneOffset.UTC);
	};
	
	private Optional<LocalDateTime> extractDateTimeOriginal(final File imageFile, Function<File, LocalDateTime>... extractorFuctions) {
		return Arrays.stream(extractorFuctions).map(extractorFunction -> {
			return extractorFunction.apply(imageFile);
		}).filter(dateTimeOriginal -> dateTimeOriginal!=null).findFirst();
	}
	
    private Path buildArchiveFilename(LocalDateTime dateTimeOriginal, String sha1, String extension) {
    	String folderInArchive = String.format("%04d/%02d", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue());    	    	
        String fileName = String.format("%04d-%02d-%02d--%02d-%02d-%02d---%s%s", dateTimeOriginal.getYear(), dateTimeOriginal.getMonthValue(), dateTimeOriginal.getDayOfMonth(),
                dateTimeOriginal.getHour(), dateTimeOriginal.getMinute(), dateTimeOriginal.getSecond(), sha1, extension);
        return Paths.get(this.archive.getPath(), folderInArchive, fileName);
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

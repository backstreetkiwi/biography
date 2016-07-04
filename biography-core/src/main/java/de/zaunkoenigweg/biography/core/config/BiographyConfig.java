package de.zaunkoenigweg.biography.core.config;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiographyConfig {

	public static final String KEY_IMPORT_FOLDER = "biography.import.folder";
    public static final String KEY_ARCHIVE_FOLDER = "biography.archive.folder";

	private static final Logger LOG = LogManager.getLogger(BiographyConfig.class);

    private File importFolder;
    private File archiveFolder;

    /**
     * Reads Biography configuration from system properties.
     * 
     * @throws IllegalArgumentException if given system properties are not valid.
     */
	public BiographyConfig() {

		String importFolderProperty = System.getProperty(KEY_IMPORT_FOLDER);
		String archiveFolderProperty = System.getProperty(KEY_ARCHIVE_FOLDER);
		
		if(importFolderProperty==null) {
			throw new IllegalArgumentException("Import folder property not set.");
		}
		if(archiveFolderProperty==null) {
			throw new IllegalArgumentException("Archive folder property not set.");
		}
		
		importFolder = new File(importFolderProperty);
		archiveFolder = new File(archiveFolderProperty);
		if(importFolder==null || !importFolder.exists() || !importFolder.isDirectory()) {
			throw new IllegalArgumentException(String.format("Import folder '%s' does not exist.", importFolderProperty));
		}
		if(archiveFolder==null || !archiveFolder.exists() || !archiveFolder.isDirectory()) {
			throw new IllegalArgumentException(String.format("Archive folder '%s' does not exist.", archiveFolderProperty));
		}
		if(importFolder.equals(archiveFolder)) {
			throw new IllegalArgumentException(String.format("Import folder '%s' must not be same as archive folder.", archiveFolderProperty));
		}
		LOG.info(String.format("Biography configuration: import-folder='%s', archive-folder='%s'", this.importFolder, this.archiveFolder));
	}

	public File getImportFolder() {
		return importFolder;
	}
	
	public File getArchiveFolder() {
		return archiveFolder;
	}
    
    
	

	
}

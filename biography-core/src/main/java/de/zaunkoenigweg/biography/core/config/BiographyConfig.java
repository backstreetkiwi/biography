package de.zaunkoenigweg.biography.core.config;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;

public class BiographyConfig {

    private final static Log LOG = LogFactory.getLog(BiographyConfig.class);

    private File importFolder;
    private File archiveFolder;
    private File indexFolder;

    private String importFolderProperty;
    private String archiveFolderProperty;
    private String indexFolderProperty;
    
    private String databaseName;

    /**
     * Creates Biography configuration using properties.
     * 
     * @throws BeanInitializationException if Biography configuration cannot be read. 
     */
    @PostConstruct
    public void init() {

        if(importFolderProperty==null) {
            String msg = "Import folder property is not set.";
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }
        
        if(archiveFolderProperty==null) {
            String msg = "Archive folder property is not set.";
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }
        
        this.importFolder = new File(importFolderProperty);
        this.archiveFolder = new File(archiveFolderProperty);
        
        if(importFolder==null || !importFolder.exists() || !importFolder.isDirectory()) {
            String msg = String.format("Import folder '%s' does not exist or is not a directory.", importFolderProperty);
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }
        
        if(archiveFolder==null || !archiveFolder.exists() || !archiveFolder.isDirectory()) {
            String msg = String.format("Archive folder '%s' does not exist or is not a directory.", archiveFolderProperty);
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }
        
        if(importFolder.equals(archiveFolder)) {
            String msg = String.format("Import folder '%s' must not be same as archive folder.", archiveFolderProperty);
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }

        if(indexFolderProperty==null) {
            String msg = "Index folder property is not set.";
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }
        
        this.indexFolder = new File(indexFolderProperty);

        if(indexFolder==null || !indexFolder.exists() || !indexFolder.isDirectory()) {
            String msg = String.format("Index folder '%s' does not exist or is not a directory.", indexFolderProperty);
            LOG.error(msg);
            throw new BeanInitializationException(msg);
        }
        
        LOG.info(String.format("Biography configuration: import-folder='%s', archive-folder='%s', database-name='%s'", this.importFolder, this.archiveFolder, this.databaseName));
    }

    public void setImportFolderProperty(String importFolderProperty) {
        this.importFolderProperty = importFolderProperty;
    }
    
    public void setArchiveFolderProperty(String archiveFolderProperty) {
        this.archiveFolderProperty = archiveFolderProperty;
    }
    
    public void setIndexFolderProperty(String indexFolderProperty) {
        this.indexFolderProperty = indexFolderProperty;
    }
    
	public File getImportFolder() {
		return importFolder;
	}
	
	public File getArchiveFolder() {
		return archiveFolder;
	}

    public File getIndexFolder() {
        return indexFolder;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

}

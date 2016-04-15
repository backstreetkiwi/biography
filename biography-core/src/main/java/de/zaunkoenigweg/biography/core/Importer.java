package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.FileFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Importer {

    private static final Logger LOG = LogManager.getLogger(Importer.class);

    private File importFolder;
    private File archive;
    
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

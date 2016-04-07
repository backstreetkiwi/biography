package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.io.FileFilter;

public class Importer {

    private File importFolder;
    private File archive;
    
    private boolean isInitialized() {
        if(this.importFolder==null) {
            return false;
        }
        if(this.archive==null) {
            return false;
        }
        if(!(this.importFolder.exists() && this.importFolder.isDirectory())) {
            return false;
        }
        if(!(this.archive.exists() && this.archive.isDirectory())) {
            return false;
        }
        if(this.archive.equals(this.importFolder)) {
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
        for (int i = 0; i < imageFiles.length; i++) {
            // TODO import
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

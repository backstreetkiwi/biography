package de.zaunkoenigweg.biography.core;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        String propertyImportFolder = System.getProperty("biography.import.folder");
        String propertyArchiveFolder = System.getProperty("biography.archive.folder");
        Importer importer = new Importer();
        importer.setImportFolder(new File(propertyImportFolder));
        importer.setArchive(new File(propertyArchiveFolder));
        importer.importAll();
    }

}

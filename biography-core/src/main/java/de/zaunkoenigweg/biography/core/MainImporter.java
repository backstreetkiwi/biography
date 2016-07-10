package de.zaunkoenigweg.biography.core;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainImporter {

	private static final Logger LOG = LogManager.getLogger(MainImporter.class);

	public static void main(String[] args) {
        LOG.info("Biography importer started.");
        String propertyImportFolder = System.getProperty("biography.import.folder");
        String propertyArchiveFolder = System.getProperty("biography.archive.folder");
        Importer importer = new Importer();
        importer.setImportFolder(new File(propertyImportFolder));
        importer.setArchive(new File(propertyArchiveFolder));
        importer.importAll();
        LOG.info("Biography importer finished.");
	}

}

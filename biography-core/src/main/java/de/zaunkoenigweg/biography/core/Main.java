package de.zaunkoenigweg.biography.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.db.Database;

public class Main {

	private static final Logger LOG = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		LOG.info("Rebuild database...");
    	Database database = new Database();
    	database.setBiographyConfig(new BiographyConfig());
    	database.init();
    	database.rebuild();
        LOG.info(String.format("Biography database containing %d files.", database.getFileCount()));
    	database.close();
		LOG.info("Database successfully rebuilt.");
		
	}

}

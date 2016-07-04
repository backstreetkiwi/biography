package de.zaunkoenigweg.biography.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.db.Database;

public class Status {
    
    private static final Logger LOG = LogManager.getLogger(Status.class);

    public static void main(String[] args) {
    	Database database = new Database();
    	database.setBiographyConfig(new BiographyConfig());
    	database.init();
    	long databaseFileCount = database.getFileCount();
    	database.close();
        LOG.info(String.format("Biography database containing %d files.", databaseFileCount));
    }

}

package de.zaunkoenigweg.biography.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.db.Database;

public class Main {

	private static final Logger LOG = LogManager.getLogger(Main.class);

	public static void main(String[] args) {

		LOG.info("Reading Configuration...");
		BiographyConfig config;
		try {
			config = new BiographyConfig();
		} catch (RuntimeException e) {
			LOG.error("Configuration could not be read!");
			return;
		}
		LOG.info("Configuration successfully read...");

		LOG.info("Rebuild database...");
		Database database = new Database();
		database.setBiographyConfig(config);
		database.init();
		database.rebuild();
		database.close();
		LOG.info("Database successfully rebuilt.");
	}

}

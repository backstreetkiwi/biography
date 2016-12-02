package de.zaunkoenigweg.biography.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.db.Database;

public class MainRebuildDatabase {
    
    private final static Log LOG = LogFactory.getLog(MainRebuildDatabase.class);

    public static void main(String[] args) {
        LOG.info("Biography rebuild database started...");
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        LOG.info("Spring context successfully initialized.");
        BiographyConfig config = springContext.getBean(BiographyConfig.class);
        Database database = springContext.getBean(Database.class);
        long fileCount = database.getFileCount();
        LOG.info(String.format("Biography database '%s' contains %d media files.", config.getDatabaseName(), fileCount));
        database.rebuild();
        fileCount = database.getFileCount();
        LOG.info(String.format("Biography database '%s' contains %d media files.", config.getDatabaseName(), fileCount));
        springContext.close();
        LOG.info("Biography rebuild database finished.");
    }

}

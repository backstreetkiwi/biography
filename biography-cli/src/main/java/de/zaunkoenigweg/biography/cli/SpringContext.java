package de.zaunkoenigweg.biography.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.db.Database;
import de.zaunkoenigweg.biography.core.importer.Importer;
import de.zaunkoenigweg.biography.core.index.Index;

@Configuration
@PropertySource("${biography.config.file}")
public class SpringContext {

    @Autowired
    Environment environment;
    
    @Bean
    public BiographyConfig config() {
        BiographyConfig config = new BiographyConfig();
        config.setImportFolderProperty(environment.getProperty("import.folder"));
        config.setArchiveFolderProperty(environment.getProperty("archive.folder"));
        config.setDatabaseName(environment.getProperty("database.name"));
        config.setIndexFolderProperty(environment.getProperty("index.folder"));
        return config;
    }
    
    @Bean
    public Importer importer() {
        return new Importer();
    }
    
//    @Bean
//    public Database database() {
//        return new Database();
//    }

    @Bean
    public Index index() {
        return new Index();
    }
}


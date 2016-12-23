package de.zaunkoenigweg.biography.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.importer.Importer;
import de.zaunkoenigweg.biography.core.index.Indexer;
import de.zaunkoenigweg.biography.core.index.SearchEngine;

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
        config.setIndexUrl(environment.getProperty("index.url"));
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
    public Indexer indexer() {
        return new Indexer();
    }

    @Bean
    public SearchEngine searchEngine() {
        return new SearchEngine();
    }
}


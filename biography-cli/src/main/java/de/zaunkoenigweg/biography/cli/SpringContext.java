package de.zaunkoenigweg.biography.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.zaunkoenigweg.biography.core.Importer;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;

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
        return config;
    }
    
    @Bean
    public Importer importer() {
        return new Importer();
    }
    
}

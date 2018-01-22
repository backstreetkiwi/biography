package de.zaunkoenigweg.biography.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.zaunkoenigweg.biography.core.archive.ArchiveImportService;
import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.index.ArchiveIndexingService;
import de.zaunkoenigweg.biography.core.index.ArchiveSearchService;
import de.zaunkoenigweg.biography.metadata.MetadataService;

@Configuration
@PropertySource("${biography.config.file}")
public class SpringContext {

    @Autowired
    Environment environment;
    
//    @Bean
//    public BiographyConfig config() {
//        BiographyConfig config = new BiographyConfig();
//        config.setImportFolderProperty(environment.getProperty("import.folder"));
//        config.setArchiveFolderProperty(environment.getProperty("archive.folder"));
//        config.setSolrIndexUrl(environment.getProperty("solr.url"));
//        return config;
//    }
    
//    @Bean
//    public ArchiveImportService importer() {
//        return new ArchiveImportService();
//    }

//    @Bean
//    public MetadataService metadataService() {
//        return new MetadataService();
//    }

//    @Bean
//    public ArchiveValidationService archiveValidationService() {
//        return new ArchiveValidationService();
//    }

//    @Bean
//    public ArchiveMetadataService archiveMetadataService() {
//        return new ArchiveMetadataService();
//    }

//    @Bean
//    public ArchiveIndexingService archiveIndexingService() {
//        return new ArchiveIndexingService();
//    }

//    @Bean
//    public ArchiveSearchService archiveSearchService() {
//        return new ArchiveSearchService();
//    }
}


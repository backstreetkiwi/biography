package de.zaunkoenigweg.biography.web;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = { "de.zaunkoenigweg.biography.metadata", "de.zaunkoenigweg.biography.core", "de.zaunkoenigweg.biography.web" })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }	
    
    @Bean
    public File importFolder(@Value("${import.path}") String importPath, File archiveFolder) {
    		File importFolder = new File(importPath);
    		if(!importFolder.exists()) {
    			String msg = String.format("The import folder %s does not exist", importPath);
    			throw new RuntimeException(msg);
    		}
    		if(archiveFolder.equals(importFolder)) {
    			String msg = String.format("The import folder (%s) must not be the same as the archive folder (%s)", importPath, archiveFolder.getAbsolutePath());
    			throw new RuntimeException(msg);
    		}
    		return importFolder;
    }

    @Bean
    public File archiveFolder(@Value("${archive.path}") String archivePath) {
    		File archiveFolder = new File(archivePath);
    		if(!archiveFolder.exists()) {
    			String msg = String.format("The archive folder %s does not exist", archivePath);
    			throw new RuntimeException(msg);
    		}
    		return archiveFolder;
    }

    @Bean
    public String solrIndexUrl(@Value("${solr.index.url}") String url) {
    		if(StringUtils.isBlank(url)) {
    			String msg = "The Solr index url must not be empty.";
    			throw new RuntimeException(msg);
    		}
    		return url;
    }
}

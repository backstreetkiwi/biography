package de.zaunkoenigweg.biography.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.zaunkoenigweg.biography.core.Importer;

public class MainImport {
    
    private final static Log LOG = LogFactory.getLog(MainImport.class);

    public static void main(String[] args) {
        LOG.info("Biography importer started...");
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        LOG.info("Spring context successfully initialized.");
        Importer importer = springContext.getBean(Importer.class);
        importer.importAll();
        LOG.info("Biography importer finished.");
    }

}

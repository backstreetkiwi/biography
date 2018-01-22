package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.archive.ArchiveImportService;

public class MainImport {
    
    private final static Log LOG = LogFactory.getLog(MainImport.class);

    public static void main(String[] args) {
//        LOG.info("Biography importer started...");
//        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
//        LOG.info("Spring context successfully initialized.");
//
//        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));
//        
//        String input = null;
//        
//        System.out.print("Enter 'nodry' for NOT doing a dry run!: ");
//        try {
//            input = stdInReader.readLine();
//        } catch (IOException e) {
//            LOG.error("Read from System.in failed.", e);
//            return;
//        }
//        
//        boolean dry = !"nodry".equals(input);
//        
//        ArchiveImportService importer = springContext.getBean(ArchiveImportService.class);
//        importer.importAll(dry);
//        springContext.close();
//        LOG.info("Biography importer finished.");
    }

}

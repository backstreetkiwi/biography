package de.zaunkoenigweg.biography.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class MainRebuildIndex {
    
    private final static Log LOG = LogFactory.getLog(MainRebuildIndex.class);

    private static de.zaunkoenigweg.biography.core.index.ArchiveIndexingService archiveIndexingService;

    public static void main(String[] args) {
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);

        archiveIndexingService = springContext.getBean(de.zaunkoenigweg.biography.core.index.ArchiveIndexingService.class);
        
        rebuildIndex();

        springContext.close();
        System.out.println("Bye...");
    }

    private static void rebuildIndex() {
    		archiveIndexingService.rebuildIndex();
    }
}

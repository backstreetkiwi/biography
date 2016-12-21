package de.zaunkoenigweg.biography.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.index.Index;

public class MainDumpArchive {
    
    private final static Log LOG = LogFactory.getLog(MainDumpArchive.class);

    public static void main(String[] args) {
        LOG.info("Biography dump started...");
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        LOG.info("Spring context successfully initialized.");
        Index index = springContext.getBean(Index.class);
        index.dumpArchiveInfo();
        System.out.printf("The archive contains %d media files.%n", index.getMediaFileCount());
        springContext.close();
        LOG.info("Biography dump finished.");
    }

}

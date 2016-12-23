package de.zaunkoenigweg.biography.cli;

import java.io.PrintStream;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.archive.ArchiveInfo;
import de.zaunkoenigweg.biography.core.index.SearchEngine;

public class MainDumpArchive {

    private final static Log LOG = LogFactory.getLog(MainDumpArchive.class);

    public static void main(String[] args) throws InterruptedException {
        LOG.info("Biography dump started...");
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        LOG.info("Spring context successfully initialized.");
        SearchEngine searchEngine = springContext.getBean(SearchEngine.class);
        ArchiveInfo archiveInfo = searchEngine.getArchiveInfo();
        archiveInfo.years().forEach(year -> {
            System.out.printf("Year %04d (%d)%n", year.getValue(), archiveInfo.getCount(year));
            archiveInfo.months(year).forEach(month -> {
                System.out.printf("   %s (%d)%n", month.getMonth(), archiveInfo.getCount(month));
            });
        });
        System.out.println("-------------------");
        System.out.printf("Total: %d%n", archiveInfo.getCount());

        System.out.println();
        System.out.println();
        archiveInfo.albums()
                   .forEach(album -> {
                       System.out.println(album);
                       album.chapters()
                            .forEach(indentingPrintln(System.out, 4));
                       System.out.println();
                   });
        System.out.println();

        Thread.sleep(3000);

        springContext.close();
        LOG.info("Biography dump finished.");
    }
    
    private static Consumer<Object> indentingPrintln(PrintStream printStream, int indent) {
        return (string) -> printStream.println(StringUtils.repeat(' ', indent) + string);
    }

}

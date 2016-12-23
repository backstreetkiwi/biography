package de.zaunkoenigweg.biography.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import de.zaunkoenigweg.biography.core.index.SearchEngine;

public class MainFindByDate {
    
    private final static Log LOG = LogFactory.getLog(MainFindByDate.class);

    public static void main(String[] args) {
        LOG.info("Biography index started...");
        AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(SpringContext.class);
        LOG.info("Spring context successfully initialized.");
        SearchEngine searchEngine = springContext.getBean(SearchEngine.class);
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        String query = null;
        
        try {
            System.out.print("Query: ");
            query = br.readLine();
            while(StringUtils.isNotBlank(query)) {
                LocalDate localDate = LocalDate.parse(query);
                searchEngine.findByDate(LocalDateTime.of(localDate, LocalTime.MIDNIGHT)).forEach(System.out::println);
                System.out.print("Query: ");
                query = br.readLine();
            }
        } catch (IOException e) {
            LOG.error("Read from System.in failed.", e);
        }
        finally {
            springContext.close();
            LOG.info("Biography index finished.");
        }
    }

}

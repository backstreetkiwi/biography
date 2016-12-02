package de.zaunkoenigweg.biography.core.index;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.TimestampExtractor;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Index {

    private final static Log LOG = LogFactory.getLog(Index.class);

    @Autowired
    private BiographyConfig config;

    @PostConstruct
    public void init() {
        LOG.info(String.format("Index initialized, directory '%s'.", config.getIndexFolder().getAbsolutePath()));
    }

    @PreDestroy
    public void close() {
        LOG.info("Index stopped.");
    }

    public void index() {
        List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());
        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = FSDirectory.open(config.getIndexFolder().toPath());
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            final IndexWriter iwriter = new IndexWriter(directory, config);
            iwriter.deleteAll();
            mediaFiles.stream()
                .map(fileToDocumentMapper)
                .forEach(doc -> {
                    try {
                        iwriter.addDocument(doc);
                    } catch (IOException e) {
                        LOG.error("Document could not be written to Lucene index.");
                        LOG.error(e);
                    }
                });
            iwriter.close();
        } catch (IOException e) {
            LOG.error(String.format("Index could not be initialized in directory '%s'.", config.getIndexFolder().getAbsolutePath()));
            e.printStackTrace();
        }
        
        LOG.info("Index rebuilt, containing %d media files.");
    }
    
    public void findInDescription(String text) {
        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = FSDirectory.open(config.getIndexFolder().toPath());
            DirectoryReader ireader = DirectoryReader.open(directory);
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser("description", analyzer);
            Query query = parser.parse(text);
            ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
            if(hits.length==0) {
                System.out.println("Keine Treffer ...");
            }
            // Iterate through the results:
            for (int i = 0; i < hits.length; i++) {
              Document hitDoc = isearcher.doc(hits[i].doc);
              System.out.printf("%04d: %s (%s)%n", i, hitDoc.get("description"), hitDoc.get("fileName"));
            }
            ireader.close();
            directory.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    
    }
    
    private Function<File, Document> fileToDocumentMapper = (file) -> {
        LOG.info(String.format("Mapping file '%s' to Lucene document.", file.getName()));
        Optional<MediaFileType> fileType = MediaFileType.of(file);
        if(!fileType.isPresent()) {
            // TODO deal with other file types
            return null;
        }
        TimestampExtractor timestampExtractor = fileType.get().getTimestampExtractorForArchivedFiles();
        LocalDateTime dateTime = timestampExtractor.apply(file);
        if(dateTime==null) {
            // TODO deal with it
            return null;
        }
        Document document = new Document();
        document.add(new StringField("fileName", file.getName(), Store.YES));
        if(ExifData.supports(fileType.get())) {
            ExifData exifData = ExifData.of(file);
            if(exifData!=null) {
                if(exifData.getDescription().isPresent()) {
                    document.add(new TextField("description", exifData.getDescription().get(), Store.YES));
                }
            }
        }
        System.out.println(document);
        return document;
    };  
    
}

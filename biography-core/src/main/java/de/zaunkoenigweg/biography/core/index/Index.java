package de.zaunkoenigweg.biography.core.index;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
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

    private final static ToLongFunction<LocalDateTime> DATETIME_ORIGINAL_TO_LONG_POINT = datetimeOriginal -> {
        return datetimeOriginal.getYear() * 10000 + datetimeOriginal.getMonthValue() * 100 + datetimeOriginal.getDayOfMonth();
    };

    private final static Function<File, Document> MEDIA_FILE_TO_INDEXED_DOCUMENT = (file) -> {
        LOG.info(String.format("Mapping file '%s' to Lucene document.", file.getName()));
        Optional<MediaFileType> fileType = MediaFileType.of(file);
        if(!fileType.isPresent()) {
            LOG.warn(String.format("No valid media file type could be found for '%s'", file.getAbsolutePath()));
            return null;
        }
        TimestampExtractor timestampExtractor = fileType.get().getTimestampExtractorForArchivedFiles();
        LocalDateTime dateTime = timestampExtractor.apply(file);
        if(dateTime==null) {
            LOG.warn(String.format("No valid timestamp could be found for '%s'", file.getAbsolutePath()));
            return null;
        }
        Document document = new Document();
        document.add(new StringField("fileName", file.getName(), Store.YES));
        document.add(new LongPoint("datetimeOriginal", DATETIME_ORIGINAL_TO_LONG_POINT.applyAsLong(dateTime)));
        if(ExifData.supports(fileType.get())) {
            ExifData exifData = ExifData.of(file);
            if(exifData!=null) {
                if(exifData.getDescription().isPresent()) {
                    document.add(new TextField("description", exifData.getDescription().get(), Store.YES));
                }
// TODO Index of albums                
//                System.out.println(file.getName());
//                Optional<String> userComment = exifData.getUserComment();
//                System.out.println(userComment);
//                if(userComment.isPresent()) {
//                    BiographyMetadata biographyMetadata = BiographyMetadata.from(userComment.get());
//                    if(biographyMetadata!=null) {
//                        biographyMetadata.getAlbums().stream().forEach(album -> {System.out.println(album.getTitle() + " " + album.getChapter());});
//                    } else {
//                        System.out.println("metadata==null");
//                    }
//                }
//                System.out.println("\n");
            }
        }
        return document;
    };  

    @PostConstruct
    public void init() {
        LOG.info(String.format("Index initialized, directory '%s'.", getIndexFolderMediaFiles().getAbsolutePath()));
    }

    @PreDestroy
    public void close() {
        LOG.info("Index stopped.");
    }

    /**
     * Creates an index of all Media Files
     */
    public void index() {
        List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());
        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = FSDirectory.open(getIndexFolderMediaFiles().toPath());
            final IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
            indexWriter.deleteAll();
            mediaFiles.stream()
                .map(MEDIA_FILE_TO_INDEXED_DOCUMENT)
                .forEach(doc -> {
                    try {
                        indexWriter.addDocument(doc);
                    } catch (IOException e) {
                        LOG.error("Document could not be written to Lucene index.");
                        LOG.error(e);
                    }
                });
            indexWriter.close();
        } catch (IOException e) {
            LOG.error(String.format("Index could not be initialized in directory '%s'.", getIndexFolderMediaFiles().getAbsolutePath()));
            e.printStackTrace();
        }
        
        LOG.info("Index rebuilt, containing %d media files.");
    }
    
    private void findInMediaFileIndex(Supplier<Query> querySupplier) {
        try {
            Directory directory = FSDirectory.open(getIndexFolderMediaFiles().toPath());
            DirectoryReader directoryReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
            Query query = querySupplier.get();
            if(query==null) {
                System.out.println("Query could not be initialized :-(");
                return;
            }
            ScoreDoc[] hits = indexSearcher.search(query, 1000).scoreDocs;
            if(hits.length==0) {
                System.out.println("No media file matches your query :-(");
            }
            // Iterate through the results:
            for (int i = 0; i < hits.length; i++) {
              Document hitDoc = indexSearcher.doc(hits[i].doc);
              System.out.printf("%04d: %s (%s)%n", i, hitDoc.get("description"), hitDoc.get("fileName"));
            }
            directoryReader.close();
            directory.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    
    }
    
    public void findInDescription(String text) {
        QueryParser parser = new QueryParser("description", new StandardAnalyzer());
        findInMediaFileIndex(()-> {
            try {
                return parser.parse(text);
            } catch (ParseException e) {
                return null;
            }
        });
    }
    
    public void findByDate(LocalDateTime from, LocalDateTime to) {
        long fromLongPoint = DATETIME_ORIGINAL_TO_LONG_POINT.applyAsLong(from);
        long toLongPoint = DATETIME_ORIGINAL_TO_LONG_POINT.applyAsLong(to);
        findInMediaFileIndex(()-> LongPoint.newRangeQuery("datetimeOriginal", fromLongPoint, toLongPoint));
    }
    
    public void findByDate(LocalDateTime date) {
        long dateLongPoint = DATETIME_ORIGINAL_TO_LONG_POINT.applyAsLong(date);
        findInMediaFileIndex(()-> LongPoint.newExactQuery("datetimeOriginal", dateLongPoint));
    }
    
    private File getIndexFolderMediaFiles() {
        return new File(config.getIndexFolder(), "mediafiles");
    }

    private File getIndexFolderAlbums() {
        return new File(config.getIndexFolder(), "albums");
    }
}

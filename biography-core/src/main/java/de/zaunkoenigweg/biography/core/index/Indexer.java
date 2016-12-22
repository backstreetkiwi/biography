package de.zaunkoenigweg.biography.core.index;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.TimestampExtractor;
import de.zaunkoenigweg.biography.core.archive.AlbumInfo;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Indexer {

    private final static Log LOG = LogFactory.getLog(Indexer.class);

    @Autowired
    private BiographyConfig config;

    private final static ToLongFunction<LocalDateTime> DATETIME_ORIGINAL_TO_LONG_POINT = datetimeOriginal -> {
        return datetimeOriginal.getYear() * 10000 + datetimeOriginal.getMonthValue() * 100 + datetimeOriginal.getDayOfMonth();
    };

    private final static Function<File, SolrInputDocument> MEDIA_FILE_TO_SOLR_DOCUMENT = (file) -> {

        LOG.info(String.format("Mapping file '%s' to JSON document.", file.getName()));

        Optional<MediaFileType> fileType = MediaFileType.of(file);

        if (!fileType.isPresent()) {
            LOG.error(String.format("No valid media file type could be found for '%s'", file.getAbsolutePath()));
            return null;
        }

        TimestampExtractor timestampExtractor = fileType.get().getTimestampExtractorForArchivedFiles();
        LocalDateTime dateTime = timestampExtractor.apply(file);
        if (dateTime == null) {
            LOG.error(String.format("No valid timestamp could be found for '%s'", file.getAbsolutePath()));
            return null;
        }
        String imageDescription = null;
        Set<String> albumTitles = null;
        Set<String> albumChapters = null;

        if (ExifData.supports(fileType.get())) {
            ExifData exifData = ExifData.of(file);
            if (exifData != null) {
                if (exifData.getDescription().isPresent()) {
                    imageDescription = StringUtils.replace(exifData.getDescription().get(), "\"", "\\\"");
                }
                Optional<String> userComment = exifData.getUserComment();
                if (userComment.isPresent()) {
                    BiographyMetadata biographyMetadata = BiographyMetadata.from(userComment.get());
                    if (biographyMetadata != null) {
                        albumTitles = biographyMetadata.getAlbums()
                                                       .stream()
                                                       .map(Album::getTitle)
                                                       .collect(Collectors.toSet());
                        albumChapters = biographyMetadata.getAlbums()
                                                         .stream()
                                                         .map(Album::getId)
                                                         .collect(Collectors.toSet());
                    }
                }
            }
        }

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", file.getName());
        document.addField("description", imageDescription);
        document.addField("albumTitles", albumTitles);
        document.addField("albumChapters", albumChapters);
        document.addField("dateOriginal", DATETIME_ORIGINAL_TO_LONG_POINT.applyAsLong(dateTime));
        document.addField("dateTimeOriginal", dateTime.toString());
        document.addField("year", dateTime.getYear());
        document.addField("month", dateTime.getMonthValue());
        document.addField("day", dateTime.getDayOfMonth());

        return document;
    };

    @PostConstruct
    public void init() {
        LOG.info(String.format("Index initialized, Solr URL is '%s'.", config.getIndexUrl()));
    }

    @PreDestroy
    public void close() {
        LOG.info("Index stopped.");
    }

    /**
     * Creates an index of all Media Files
     */
    public void index() {

        try {

            SolrClient solr = new HttpSolrClient.Builder(config.getIndexUrl()).build();

            UpdateResponse deleteByQuery = solr.deleteByQuery("*:*");
            LOG.info(String.format("Deleted all rows in %s -> Status %d", config.getIndexUrl(), deleteByQuery.getStatus()));

            List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());

            mediaFiles.stream()
                      .map(MEDIA_FILE_TO_SOLR_DOCUMENT)
                      .forEach(document -> {
                          try {
                              UpdateResponse response = solr.add(document);
                              LOG.trace(response);
                          } catch (IOException | SolrServerException e) {
                              LOG.error("Document could not be written to Solr.");
                              LOG.error(e);
                          }
                      });

            solr.commit();

        } catch (SolrServerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Creates an index of all Media Files
     */
    public void defineIndex() {

        try {

            SolrClient solr = new HttpSolrClient.Builder(config.getIndexUrl()).build();

            Map<String, Map<String, Object>> FIELD_DEFINITIONS = new HashMap<>();
            Map<String, Object> fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "fileName");
            fieldAttributes.put("type", "string");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("fileName", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "albumTitles");
            fieldAttributes.put("type", "string");
            fieldAttributes.put("multiValued", Boolean.TRUE);
            FIELD_DEFINITIONS.put("albumTitles", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "albumChapters");
            fieldAttributes.put("type", "string");
            fieldAttributes.put("multiValued", Boolean.TRUE);
            FIELD_DEFINITIONS.put("albumChapters", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "description");
            fieldAttributes.put("type", "text_general");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("description", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "year");
            fieldAttributes.put("type", "long");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("year", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "dateOriginal");
            fieldAttributes.put("type", "long");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("dateOriginal", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "dateTimeOriginal");
            fieldAttributes.put("type", "string");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("dateTimeOriginal", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "month");
            fieldAttributes.put("type", "long");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("month", fieldAttributes);

            fieldAttributes = new HashMap<>();
            fieldAttributes.put("name", "day");
            fieldAttributes.put("type", "long");
            fieldAttributes.put("multiValued", Boolean.FALSE);
            FIELD_DEFINITIONS.put("day", fieldAttributes);

            UpdateResponse deleteByQuery = solr.deleteByQuery("*:*");
            LOG.info(String.format("Deleted all rows in %s -> Status %d", config.getIndexUrl(), deleteByQuery.getStatus()));

            FIELD_DEFINITIONS.forEach((name, attributes) -> {
                try {
                    NamedList<Object> request = solr.request(new SchemaRequest.DeleteField(name));
                    LOG.info(String.format("Deleted field '%s' -> Response %s", name, request));
                } catch (SolrServerException | IOException e) {
                    LOG.error(String.format("Field named '%s' could not be deleted.", name), e);
                }
            });

            FIELD_DEFINITIONS.forEach((name, attributes) -> {
                try {
                    NamedList<Object> response = solr.request(new SchemaRequest.AddField(attributes));
                    LOG.info(String.format("Added field '%s' -> Response %s", name, response));
                } catch (SolrServerException | IOException e) {
                    LOG.error(String.format("Field named '%s' could not be created.", name), e);
                }
            });

            solr.commit();

        } catch (SolrServerException | IOException e) {
            LOG.error("Error defining index structure.", e);
        }

    }

    public void dumpArchiveInfo() {

        Stream<Count> albumFacetCounts = streamFacetFieldCount("albumTitles");

        List<AlbumInfo> albumInfos = albumFacetCounts.map(count -> new AlbumInfo(count.getName(), count.getCount()))
                                                     .collect(Collectors.toList());

        albumInfos.forEach(album -> {
            Stream<Count> chapterFacetCounts = streamFacetFieldCount("albumChapters", q->q.setFacetPrefix(album.getName() + "|"));
            chapterFacetCounts.sorted(COMPARE_COUNT_BY_NAME)
                              .map(count -> new AlbumInfo(count.getName(), count.getCount()))
                              .forEach(album.getChapters()::add);
            album.getChapters().forEach(chapter -> {
                chapter.setStartDate(query(createQueryBoundaryDateForFacet("albumChapters", chapter.getName(), ORDER.asc), EXTRACT_DATE_OF_FIRST_DOCUMENT));
                chapter.setEndDate(query(createQueryBoundaryDateForFacet("albumChapters", chapter.getName(), ORDER.desc), EXTRACT_DATE_OF_FIRST_DOCUMENT));
            });
            album.setStartDate(query(createQueryBoundaryDateForFacet("albumTitles", album.getName(), ORDER.asc), EXTRACT_DATE_OF_FIRST_DOCUMENT));
            album.setEndDate(query(createQueryBoundaryDateForFacet("albumTitles", album.getName(), ORDER.desc), EXTRACT_DATE_OF_FIRST_DOCUMENT));
        });

        albumInfos.stream().sorted(AlbumInfo.COMPARE_BY_START_DATE).forEach(album -> {
            System.out.println(album);
        });

        System.out.println();

        Stream<Count> yearFacetCounts = streamFacetFieldCount("year");
        yearFacetCounts.sorted(COMPARE_COUNT_BY_NAME)
                       .forEach(count -> {
                           System.out.println(count);
                           Stream<Count> monthFacetCounts = streamFacetFieldCount("month", q->q.setQuery("year:"+count.getName()));
                           monthFacetCounts.filter(COUNT_NOT_EMPTY)
                                           .sorted(COMPARE_COUNT_BY_NAME_AS_INT)
                                           .forEach(c -> System.out.println("  " + Month.values()[Integer.valueOf(c.getName())-1] + "(" + c.getCount() + ")"));
                           System.out.println();
                       });

    }
    
    private Predicate<Count> COUNT_NOT_EMPTY = count -> count.getCount() > 0;
    
    private Comparator<Count> COMPARE_COUNT_BY_NAME = Comparator.comparing(Count::getName);

    private Comparator<Count> COMPARE_COUNT_BY_NAME_AS_INT = Comparator.comparingInt(count -> Integer.valueOf(count.getName()));

    private static final Function<QueryResponse, LocalDate> EXTRACT_DATE_OF_FIRST_DOCUMENT = response -> LocalDateTime.parse(
            response.getResults().get(0).get("dateTimeOriginal").toString()).toLocalDate();

    private SolrQuery createQueryBoundaryDateForFacet(String facetField, String facetValue, ORDER order) {
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("%s:\"%s\"", facetField, facetValue));
        query.setRows(1);
        query.setSort("dateOriginal", order);
        return query;
    }

    private Stream<Count> streamFacetFieldCount(String facetField) {
        return streamFacetFieldCount(facetField, q->{});
    }

    private Stream<Count> streamFacetFieldCount(String facetField, Consumer<SolrQuery> additionalQueryDefinitions) {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        query.setFacet(true);
        query.addFacetField(facetField);
        additionalQueryDefinitions.accept(query);
        return query(query, (response) -> response.getFacetField(facetField).getValues().stream());
    }

    private <R> R query(SolrQuery query, Function<QueryResponse, R> responseExtractor) {
        try {
            SolrClient solr = new HttpSolrClient.Builder(config.getIndexUrl()).build();
            QueryResponse response = solr.query(query);
            return responseExtractor.apply(response);
        } catch (SolrServerException | IOException e) {
            LOG.error("Error during Solr query.", e);
            return null;
        }
    }

    public long getMediaFileCount() {

        try {

            SolrClient solr = new HttpSolrClient.Builder(config.getIndexUrl()).build();
            SolrQuery query = new SolrQuery();
            query.setQuery("*:*");
            query.setRows(0);
            QueryResponse response = solr.query(query);
            SolrDocumentList documentList = response.getResults();
            return documentList.getNumFound();
        } catch (SolrServerException | IOException e) {
            LOG.error("Error defining index structure.", e);
            return -1;
        }

    }

    private void findInMediaFileIndex(Supplier<Query> querySupplier) {
        throw new RuntimeException("replaced by solr search queries.");
        // try {
        // Directory directory =
        // FSDirectory.open(getIndexFolderMediaFiles().toPath());
        // DirectoryReader directoryReader = DirectoryReader.open(directory);
        // IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        // Query query = querySupplier.get();
        // if(query==null) {
        // System.out.println("Query could not be initialized :-(");
        // return;
        // }
        // ScoreDoc[] hits = indexSearcher.search(query, 1000).scoreDocs;
        // if(hits.length==0) {
        // System.out.println("No media file matches your query :-(");
        // }
        // // Iterate through the results:
        // for (int i = 0; i < hits.length; i++) {
        // Document hitDoc = indexSearcher.doc(hits[i].doc);
        // System.out.printf("%04d: %s (%s)%n", i, hitDoc.get("description"),
        // hitDoc.get("fileName"));
        // }
        // directoryReader.close();
        // directory.close();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    public void findInDescription(String text) {
        QueryParser parser = new QueryParser("description", new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        findInMediaFileIndex(() -> {
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
        findInMediaFileIndex(() -> LongPoint.newRangeQuery("datetimeOriginal", fromLongPoint, toLongPoint));
    }

    public void findByDate(LocalDateTime date) {
        long dateLongPoint = DATETIME_ORIGINAL_TO_LONG_POINT.applyAsLong(date);
        findInMediaFileIndex(() -> LongPoint.newExactQuery("datetimeOriginal", dateLongPoint));
    }

}

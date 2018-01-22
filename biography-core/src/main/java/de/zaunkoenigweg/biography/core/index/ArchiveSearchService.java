package de.zaunkoenigweg.biography.core.index;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Component;

@Component
public class ArchiveSearchService {

    private final static Log LOG = LogFactory.getLog(ArchiveSearchService.class);

    private String solrIndexUrl;

//    private final static Predicate<Count> COUNT_NOT_EMPTY = count -> count.getCount() > 0;
//    private final static Comparator<Count> COMPARE_COUNT_BY_NUMERIC_NAME = Comparator.comparingInt(count -> Integer.valueOf(count.getName()));
//
//    private static final Function<QueryResponse, LocalDate> EXTRACT_DATE_OF_FIRST_DOCUMENT = response -> LocalDateTime.parse(
//            response.getResults().get(0).get(Index.FIELD_DATE_TIME_ORIGINAL).toString()).toLocalDate();
    
	public ArchiveSearchService(String solrIndexUrl) {
		this.solrIndexUrl = solrIndexUrl;
		LOG.info("ArchiveSearchService started.");
		LOG.info(String.format("solrIndexUrl=%s", this.solrIndexUrl));
	}

    @PreDestroy
    public void close() {
        LOG.info("Index stopped.");
    }

    public Stream<String> findByDescription(String queryString) {
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("description:\"%s\"", queryString));
        query.setRows(1000);
        return query(query, response -> response.getResults().stream().map(doc -> String.format("%s -> '%s'", doc.get(Index.FIELD_ID), doc.get(Index.FIELD_DESCRIPTION))));
    }
    
    public Stream<String> findByDate(LocalDateTime dateTime) {
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("dateOriginal:%s", Index.toLongPoint(dateTime)));
        query.setRows(1000);
        return query(query, response -> response.getResults().stream().map(doc -> String.format("%s -> '%s'", doc.get(Index.FIELD_ID), doc.get(Index.FIELD_DESCRIPTION))));
    }
    
//    public ArchiveInfo getArchiveInfo() {
//        ArchiveInfo archiveInfo = new ArchiveInfo();
//        addDateRelatedInfo(archiveInfo);
//        addAlbumRelatedInfo(archiveInfo);
//        return archiveInfo;
//    }
//
//    private void addDateRelatedInfo(ArchiveInfo archiveInfo) {
//        BiFunction<String, String, YearMonth> stringsToYearMonth = (year, month) -> YearMonth.of(Integer.valueOf(year), Integer.valueOf(month));
//        streamFacets(Index.FIELD_YEAR).sorted(COMPARE_COUNT_BY_NUMERIC_NAME)
//                                      .forEach(yearFacet -> {
//                                          Stream<Count> monthFacetCounts = streamFacets(Index.FIELD_MONTH, q -> q.setQuery(Index.queryString(Index.FIELD_YEAR, yearFacet.getName())));
//                                          monthFacetCounts.filter(COUNT_NOT_EMPTY)
//                                                          .sorted(COMPARE_COUNT_BY_NUMERIC_NAME)
//                                                          .forEach(monthFacet -> archiveInfo.setCount(stringsToYearMonth.apply(yearFacet.getName(), monthFacet.getName()), monthFacet.getCount()));
//                                      });
//    }
//    
//    private void addAlbumRelatedInfo(ArchiveInfo archiveInfo) {
//        
//        streamFacets(Index.FIELD_ALBUM_TITLES).map(Count::getName)
//                                              .map(AlbumInfo::new)
//                                              .forEach(archiveInfo::add);
//
//        archiveInfo.albums()
//                   .forEach(album -> {
//                       final String prefix = album.getName() + "|";
//                       streamFacets(Index.FIELD_ALBUM_CHAPTERS, query -> query.setFacetPrefix(prefix)).map(count -> new ChapterInfo(StringUtils.removeStart(count.getName(), prefix), count.getCount()))
//                                                                                                      .forEach(album::add);
//                       
//                       album.chapters().forEach(chapter -> {
//                             chapter.setStartDate(query(createQueryBoundaryDateForFacet(Index.FIELD_ALBUM_CHAPTERS, prefix + chapter.getName(), ORDER.asc), EXTRACT_DATE_OF_FIRST_DOCUMENT));
//                             chapter.setEndDate(query(createQueryBoundaryDateForFacet(Index.FIELD_ALBUM_CHAPTERS, prefix + chapter.getName(), ORDER.desc), EXTRACT_DATE_OF_FIRST_DOCUMENT));
//                       });
//                   });
//    }
//    
//    private SolrQuery createQueryBoundaryDateForFacet(String facetField, String facetValue, ORDER order) {
//        SolrQuery query = new SolrQuery();
//        query.setQuery(String.format("%s:\"%s\"", facetField, facetValue));
//        query.setRows(1);
//        query.setSort(Index.FIELD_DATE_ORIGINAL_LONG_POINT, order);
//        return query;
//    }

//    private Stream<Count> streamFacets(String facetField) {
//        return streamFacets(facetField, q->{});
//    }

//    private Stream<Count> streamFacets(String facetField, Consumer<SolrQuery> additionalQueryDefinitions) {
//        SolrQuery query = new SolrQuery();
//        query.setQuery("*:*");
//        query.setRows(0);
//        query.setFacet(true);
//        query.addFacetField(facetField);
//        additionalQueryDefinitions.accept(query);
//        return query(query, (response) -> response.getFacetField(facetField).getValues().stream());
//    }

    /**
     * Executes given query and maps the result with the given extractor.
     * @param query SolrQuery
     * @param responseExtractor Extracts Response Type from Query
     * @return extracted result
     */
    private <R> R query(SolrQuery query, Function<QueryResponse, R> responseExtractor) {
        try {
            SolrClient solr = new HttpSolrClient.Builder(solrIndexUrl).build();
            QueryResponse response = solr.query(query);
            return responseExtractor.apply(response);
        } catch (SolrServerException | IOException e) {
            LOG.error("Error during Solr query.", e);
            return null;
        }
    }

}

package de.zaunkoenigweg.biography.core.index;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.stereotype.Component;

@Component
public class ArchiveSearchService {

    private final static Log LOG = LogFactory.getLog(ArchiveSearchService.class);

    private String solrIndexUrl;
    
    public enum QueryMode {
        ANY, ALL;
    }

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

    public Stream<MediaFile> findByDescription(String searchString, QueryMode mode) {
        SolrQuery query = new SolrQuery();
        String queryString = Arrays.stream(StringUtils.split(searchString))
            .map(querySnippet(Index.FIELD_DESCRIPTION, mode))
            .collect(Collectors.joining(" "));
        query.setQuery(queryString);
        query.setRows(1000);
        return query(query, response -> response.getResults().stream().map(this::toFileInfo));
    }
    
    private UnaryOperator<String> querySnippet(String fieldName, QueryMode mode) {
        return token -> String.format("%s%s:%s", mode==QueryMode.ALL ? "+" : "", fieldName, token);
    }
    
    public Stream<MediaFile> findByDate(LocalDate dateTime) {
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("%s:%s", Index.FIELD_DATE_LONG_POINT, Index.localDateToLongPoint(dateTime)));
        query.setRows(1000);
        return query(query, response -> response.getResults().stream().map(this::toFileInfo));
    }
    
    public Stream<MediaFile> findByAlbum(String album) {
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("%s:\"%s\"", Index.FIELD_ALBUMS, album));
        query.setRows(1000);
        return query(query, response -> response.getResults().stream().map(this::toFileInfo));
    }
    
    @SuppressWarnings("unchecked")
    private MediaFile toFileInfo(SolrDocument doc) {
        String fileName = doc.get(Index.FIELD_ID).toString();
        String description = doc.get(Index.FIELD_DESCRIPTION)!=null ? doc.get(Index.FIELD_DESCRIPTION).toString() : null;
        List<String> albums = doc.get(Index.FIELD_ALBUMS) instanceof List<?> ? (List<String>)doc.get(Index.FIELD_ALBUMS) : null;
        return new MediaFile(fileName, description, albums);
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

    public Stream<Album> getAlbumCounts() {
        return streamFacetCounts(Index.FIELD_ALBUMS).map(this::createAlbum).sorted(Comparator.comparing(Album::getBegin));
    }
    
    private Album createAlbum(Count albumFacetCount) {
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("%s:\"%s\"", Index.FIELD_ALBUMS, albumFacetCount.getName()));
        query.setSort(Index.FIELD_DATE_LONG_POINT, ORDER.asc);
        query.setRows(1);
        LocalDate begin = query(query, response -> response.getResults().stream().map(result -> Index.longPointToLocalDate(result.get(Index.FIELD_DATE_LONG_POINT).toString())).findAny().get());
        
        query = new SolrQuery();
        query.setQuery(String.format("%s:\"%s\"", Index.FIELD_ALBUMS, albumFacetCount.getName()));
        query.setSort(Index.FIELD_DATE_LONG_POINT, ORDER.desc);
        query.setRows(1);
        LocalDate end = query(query, response -> response.getResults().stream().map(result -> Index.longPointToLocalDate(result.get(Index.FIELD_DATE_LONG_POINT).toString())).findAny().get());
        
        return new Album(albumFacetCount.getName(), albumFacetCount.getCount(), begin, end);
    }
    
    
    public Stream<Pair<LocalDate, Long>> getDayCounts(YearMonth yearMonth) {
        return streamFacetCounts(Index.FIELD_DATE_LONG_POINT, query -> {
            query.setQuery(Index.queryString(Index.FIELD_YEAR_MONTH_LONG_POINT, Index.yearMonthToLongPoint(yearMonth)));
        }).map(count-> {
            return Pair.of(Index.longPointToLocalDate(count.getName()), Long.valueOf(count.getCount()));
        }).sorted(Comparator.comparing(Pair::getLeft));
    }
    
    public Stream<Pair<YearMonth, Long>> getMonthCounts(Year year) {
        return streamFacetCounts(Index.FIELD_YEAR_MONTH_LONG_POINT, query -> {
            query.setQuery(Index.queryString(Index.FIELD_YEAR_LONG_POINT, year.toString()));
        }).map(count-> {
            return Pair.of(Index.longPointToYearMonth(count.getName()), Long.valueOf(count.getCount()));
        }).sorted(Comparator.comparing(Pair::getLeft));
    }
    
    public Stream<Pair<Year, Long>> getYearCounts() {
        return streamFacetCounts(Index.FIELD_YEAR_LONG_POINT).map(count-> {
            return Pair.of(Year.parse(count.getName()), Long.valueOf(count.getCount()));
        }).sorted(Comparator.comparing(Pair::getLeft));
    }
    
    private Stream<Count> streamFacetCounts(String facetField) {
        return streamFacetCounts(facetField, q->{});
    }

    private Stream<Count> streamFacetCounts(String facetField, Consumer<SolrQuery> additionalQueryDefinitions) {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        query.setFacet(true);
        query.addFacetField(facetField);
        additionalQueryDefinitions.accept(query);
        return query(query, (response) -> response.getFacetField(facetField).getValues().stream());
    }

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

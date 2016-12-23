package de.zaunkoenigweg.biography.core.index;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.TimestampExtractor;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Indexer {

    private final static Log LOG = LogFactory.getLog(Indexer.class);

    @Autowired
    private BiographyConfig config;

    private final static Function<File, SolrInputDocument> MEDIA_FILE_TO_SOLR_DOCUMENT = (file) -> {

        LOG.trace(String.format("Mapping file '%s' to JSON document.", file.getName()));

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

        String description = null;
        Set<String> albumTitles = null;
        Set<String> albumChapters = null;

        if (ExifData.supports(fileType.get())) {
            ExifData exifData = ExifData.of(file);
            if (exifData != null) {
                if (exifData.getDescription().isPresent()) {
                    description = StringUtils.replace(exifData.getDescription().get(), "\"", "\\\"");
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
            } else {
                // TODO
                LOG.error(String.format("Currently Biography does not work for media files without Exif data: '%s'", file.getAbsolutePath()));
                return null;
            }
        } else {
            // TODO
            LOG.error(String.format("Currently Biography does not work for media files without Exif data: '%s'", file.getAbsolutePath()));
            return null;
        }

        SolrInputDocument document = new SolrInputDocument();
        document.addField(Index.FIELD_ID, file.getName());
        document.addField(Index.FIELD_DESCRIPTION, description);
        document.addField(Index.FIELD_ALBUM_TITLES, albumTitles);
        document.addField(Index.FIELD_ALBUM_CHAPTERS, albumChapters);
        document.addField(Index.FIELD_DATE_ORIGINAL_LONG_POINT, Index.DATETIME_TO_LONG_POINT.applyAsLong(dateTime));
        document.addField(Index.FIELD_DATE_TIME_ORIGINAL, dateTime.toString());
        document.addField(Index.FIELD_YEAR, dateTime.getYear());
        document.addField(Index.FIELD_MONTH, dateTime.getMonthValue());
        document.addField(Index.FIELD_DAY, dateTime.getDayOfMonth());
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
    public boolean defineIndex() {

        try {
            SolrClient solr = new HttpSolrClient.Builder(config.getIndexUrl()).build();

            UpdateResponse deleteByQuery = solr.deleteByQuery("*:*");
            LOG.info(String.format("Deleted all rows in %s -> Status %d", config.getIndexUrl(), deleteByQuery.getStatus()));
            
            Index.fields()
            .map(Index.TO_FIELD_NAME)
            .map(SchemaRequest.DeleteField::new)
            .forEach(deleteFieldRequest -> {
                try {
                    NamedList<Object> response = solr.request(deleteFieldRequest);
                    LOG.info(String.format("Deleted field (request: '%s') -> Response %s", deleteFieldRequest, response));
                } catch (SolrServerException | IOException e) {
                    LOG.error(String.format("Error deleting field (request: '%s')", deleteFieldRequest), e);
                }
            });

            Index.fields()
            .map(SchemaRequest.AddField::new)
            .forEach(addFieldRequest -> {
                try {
                    NamedList<Object> response = solr.request(addFieldRequest);
                    LOG.info(String.format("Added field (request: '%s') -> Response %s", addFieldRequest, response));
                } catch (SolrServerException | IOException e) {
                    LOG.error(String.format("Error adding field (request: '%s')", addFieldRequest), e);
                }
            });

            solr.commit();

        } catch (SolrServerException | IOException e) {
            LOG.error("Error defining index structure.", e);
            return false;
        }
        
        return true;

    }
}

package de.zaunkoenigweg.biography.core.index;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.archive.Archive;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveValidationService;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;

/**
 * Indexing. 
 */
@Component
public class IndexingService {

	private final static Log LOG = LogFactory.getLog(IndexingService.class);

    private String solrIndexUrl;
    private Archive archive;
	private ArchiveMetadataService archiveMetadataService;
	private ArchiveValidationService archiveValidationService;

	public IndexingService(String solrIndexUrl, Archive archive, ArchiveMetadataService archiveMetadataService, ArchiveValidationService archiveValidationService) {
		this.solrIndexUrl = solrIndexUrl;
		this.archive = archive;
		this.archiveMetadataService = archiveMetadataService;
		this.archiveValidationService = archiveValidationService;
		LOG.info("IndexingService started.");
		LOG.info(String.format("solrIndexUrl=%s", this.solrIndexUrl));
	}

	/**
	 * Maps archived media file to Solr document. The archive file must be valid
	 * {@link ArchiveValidationService#isValid(File)}
	 * 
	 * @param archiveFile
	 * @return
	 */
	private final SolrInputDocument toSolrDocument(File archiveFile) {

		LOG.trace(String.format("Mapping file '%s' to JSON document.", archiveFile.getName()));

		BiographyMetadata biographyMetadata = archiveMetadataService.getMetadata(archiveFile);

		Set<String> albumTitles = null;

		albumTitles = biographyMetadata.getAlbums().stream().map(Album::getTitle).collect(Collectors.toSet());

		LocalDateTime dateTime = biographyMetadata.getDateTimeOriginal();

		SolrInputDocument document = new SolrInputDocument();
		document.addField(Index.FIELD_ID, archiveFile.getName());
		document.addField(Index.FIELD_DESCRIPTION, biographyMetadata.getDescription());
		document.addField(Index.FIELD_ALBUMS, albumTitles);
		document.addField(Index.FIELD_DATETIME_ORIGINAL, dateTime.toString());
		document.addField(Index.FIELD_DATE_LONG_POINT, Long.valueOf(Index.localDateToLongPoint(dateTime.toLocalDate())));
		document.addField(Index.FIELD_DATETIME_LONG_POINT, Long.valueOf(Index.localDateTimeToLongPoint(dateTime)));
		document.addField(Index.FIELD_YEAR_LONG_POINT, dateTime.getYear());
		document.addField(Index.FIELD_YEAR_MONTH_LONG_POINT, dateTime.getYear() * 100 + dateTime.getMonthValue());
		return document;
	};
	

	/**
	 * Creates an index of all Media Files
	 */
	public void rebuildIndex() {

		try {

			SolrClient solr = new HttpSolrClient.Builder(solrIndexUrl).build();

			UpdateResponse deleteByQuery = solr.deleteByQuery("*:*");
			LOG.info(String.format("Deleted all rows in %s -> Status %d", solrIndexUrl,
					deleteByQuery.getStatus()));

			List<File> mediaFiles = archive.mediaFiles();

			List<SolrInputDocument> documents = mediaFiles.stream().filter(archiveValidationService::isValid).map(this::toSolrDocument).collect(Collectors.toList());
			
			try {
				UpdateResponse response = solr.add(documents);
				LOG.info(response);
			} catch (IOException | SolrServerException e) {
				LOG.error("Document could not be written to Solr.");
				LOG.error(e);
			}
			

			solr.commit();

		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    /**
     * Re-indexes the given media file.
     */
    public void reIndex(File archiveFile) {

        try {

            SolrClient solr = new HttpSolrClient.Builder(solrIndexUrl).build();

            SolrInputDocument solrDocument = toSolrDocument(archiveFile);
            
            try {
            	UpdateResponse response = solr.add(solrDocument);
            	LOG.info(response);
            } catch (IOException | SolrServerException e) {
            	LOG.error("Document could not be written to Solr.");
            	LOG.error(e);
            }

            solr.commit();

        } catch (SolrServerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

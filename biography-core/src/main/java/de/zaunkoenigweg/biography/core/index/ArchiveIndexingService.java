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
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;

import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;

public class ArchiveIndexingService {

	private final static Log LOG = LogFactory.getLog(ArchiveIndexingService.class);

	@Autowired
	private BiographyConfig config;

	@Autowired
	private ArchiveMetadataService archiveMetadataService;

	@Autowired
	private ArchiveValidationService archiveValidationService;

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
		Set<String> albumChapters = null;

		albumTitles = biographyMetadata.getAlbums().stream().map(Album::getTitle).collect(Collectors.toSet());

		albumChapters = biographyMetadata.getAlbums().stream().map(Album::getId).collect(Collectors.toSet());

		LocalDateTime dateTime = biographyMetadata.getDateTimeOriginal();

		SolrInputDocument document = new SolrInputDocument();
		document.addField(Index.FIELD_ID, archiveFile.getName());
		document.addField(Index.FIELD_DESCRIPTION, biographyMetadata.getDescription());
		document.addField(Index.FIELD_ALBUM_TITLES, albumTitles);
		document.addField(Index.FIELD_ALBUM_CHAPTERS, albumChapters);
		document.addField(Index.FIELD_DATE_ORIGINAL_LONG_POINT, Index.toLongPoint(dateTime));
		document.addField(Index.FIELD_DATE_TIME_ORIGINAL, dateTime.toString());
		document.addField(Index.FIELD_YEAR, dateTime.getYear());
		document.addField(Index.FIELD_MONTH, dateTime.getMonthValue());
		document.addField(Index.FIELD_DAY, dateTime.getDayOfMonth());
		return document;
	};
	
	/**
	 * Definies the Solr index.
	 */
	public boolean defineIndex() {

		try {
			SolrClient solr = new HttpSolrClient.Builder(config.getSolrIndexUrl()).build();

			UpdateResponse deleteByQuery = solr.deleteByQuery("*:*");
			LOG.info(String.format("Deleted all rows in %s -> Status %d", config.getSolrIndexUrl(),
					deleteByQuery.getStatus()));

			Index.fields().map(Index::toFieldName).map(SchemaRequest.DeleteField::new).forEach(deleteFieldRequest -> {
				try {
					NamedList<Object> response = solr.request(deleteFieldRequest);
					LOG.info(String.format("Deleted field (request: '%s') -> Response %s", deleteFieldRequest,
							response));
				} catch (SolrServerException | IOException e) {
					LOG.error(String.format("Error deleting field (request: '%s')", deleteFieldRequest), e);
				}
			});

			Index.fields().map(SchemaRequest.AddField::new).forEach(addFieldRequest -> {
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

	/**
	 * Creates an index of all Media Files
	 */
	public void rebuildIndex() {

		try {

			SolrClient solr = new HttpSolrClient.Builder(config.getSolrIndexUrl()).build();

			UpdateResponse deleteByQuery = solr.deleteByQuery("*:*");
			LOG.info(String.format("Deleted all rows in %s -> Status %d", config.getSolrIndexUrl(),
					deleteByQuery.getStatus()));

			List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());

			mediaFiles.stream().filter(archiveValidationService::isValid).map(this::toSolrDocument)
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

}

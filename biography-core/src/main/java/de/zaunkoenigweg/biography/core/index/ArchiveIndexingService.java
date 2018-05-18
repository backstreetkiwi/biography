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

import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;

@Component
public class ArchiveIndexingService {

	private final static Log LOG = LogFactory.getLog(ArchiveIndexingService.class);

    private String solrIndexUrl;
    private File archiveFolder;
	private ArchiveMetadataService archiveMetadataService;
	private ArchiveValidationService archiveValidationService;

	public ArchiveIndexingService(String solrIndexUrl, File archiveFolder, ArchiveMetadataService archiveMetadataService, ArchiveValidationService archiveValidationService) {
		this.solrIndexUrl = solrIndexUrl;
		this.archiveFolder = archiveFolder;
		this.archiveMetadataService = archiveMetadataService;
		this.archiveValidationService = archiveValidationService;
		LOG.info("ArchiveIndexingService started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
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
		document.addField(Index.FIELD_DATE_LONG_POINT, Index.toLongPoint(dateTime));
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

			List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

			mediaFiles.stream().filter(archiveValidationService::isValid).map(this::toSolrDocument)
					.forEach(document -> {
						try {
							UpdateResponse response = solr.add(document);
							LOG.info(response);
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

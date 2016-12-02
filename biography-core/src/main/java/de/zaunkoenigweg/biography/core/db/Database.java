package de.zaunkoenigweg.biography.core.db;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.TimestampExtractor;
import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Database {

    private final static Log LOG = LogFactory.getLog(Database.class);

    private static final String COLLECTION_MEDIA_NAME = "media";
    private static final String COLLECTION_ALBUMS_NAME = "albums";
    
    @Autowired
    private BiographyConfig config;

	private MongoClient mongoClient;
	private MongoDatabase database;
	
	@PostConstruct
	public void init() {
		mongoClient = new MongoClient();
		database = mongoClient.getDatabase(config.getDatabaseName());
		LOG.info("MongoDB connection established.");
	}

	@PreDestroy
	public void close() {
		mongoClient.close();
        LOG.info("MongoDB connection closed.");
	}

	public long getFileCount() {
		return database.getCollection(COLLECTION_MEDIA_NAME).count();
	}
	
	/**
	 * Rebuilds database from scratch.
	 * 
	 * Be aware that this method deletes the old database!
	 * To just refresh the data, use {{@link #refreshDatabase()}.
	 * As of now, all the data in the database can be restored from the image files, 
	 * so the difference between the two is effectively not existing.
	 * 
	 * In later versions, the database might hold further information, such as relations between images,
	 * which might be destroyed by a full rebuild.
	 */
	public void rebuild() {
		
		MongoCollection<Document> albums = database.getCollection(COLLECTION_ALBUMS_NAME);
		albums.drop();

		MongoCollection<Document> collection = database.getCollection(COLLECTION_MEDIA_NAME);
		collection.drop();
		
		List<File> mediaFiles = BiographyFileUtils.getMediaFiles(config.getArchiveFolder());
		
        mediaFiles.stream()
                .map(fileToDocumentMapper)
                .forEach(collection::insertOne);
		
		LOG.info(String.format("Database rebuilt, containing %d media files.", collection.count()));
	}
	
//	private void addMediaFileToMonthAlbum(MongoCollection<Document> albums, Document mediaFile) {
//		String dateTimeOriginalAsString = mediaFile.get("dateTimeOriginal", String.class);
//		if(dateTimeOriginalAsString==null) {
//			return;
//		}
//		LocalDateTime dateTimeOriginal = LocalDateTime.parse(dateTimeOriginalAsString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//		String albumName = String.format("Fotos %s %s", dateTimeOriginal.getMonth(), dateTimeOriginal.getYear());
//		FindIterable<Document> findIterable = albums.find(new Document("name", albumName));
//		Document album = findIterable.first();
//		if(album==null) {
//			album = new Document("name", albumName);
//			albums.insertOne(album);
//		}
//		albums.updateOne(new Document("_id", album.get("_id")), new Document("$push", new Document("mediaFiles", mediaFile.get("_id"))));
//	}
	
	/**
	 * Refreshes database.
	 */
	public void refresh() {
		// not implemented yet
	}
	
	private Function<File, Document> fileToDocumentMapper = (file) -> {
        LOG.info(String.format("Mapping file '%s' to MongoDB document.", file.getName()));
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
		String sha1 = BiographyFileUtils.sha1(file);
		Document document = new Document("_id", sha1);
		document.append("dateTimeOriginal", dateTime.toString());
		document.append("fileName", file.getName());
        if(ExifData.supports(fileType.get())) {
            ExifData exifData = ExifData.of(file);
            if(exifData!=null) {
                if(exifData.getDescription().isPresent()) {
                    document.append("description", exifData.getDescription().get());
                }
            }
        }
		System.out.println(document);
		return document;
	};	
	
}

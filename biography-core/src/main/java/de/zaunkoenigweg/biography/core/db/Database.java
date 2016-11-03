package de.zaunkoenigweg.biography.core.db;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.ExifData;

public class Database {

	private static final Logger LOG = LogManager.getLogger(Database.class);

	// TODO Read from Configuration
	private static final String DATABASE_NAME = "biography";
	private static final String COLLECTION_MEDIA_NAME = "media";
	private static final String COLLECTION_ALBUMS_NAME = "albums";

	private BiographyConfig biographyConfig = null;
	
	private MongoClient mongoClient;
	private MongoDatabase database;

	public void setBiographyConfig(BiographyConfig biographyConfig) {
		this.biographyConfig = biographyConfig;
	}

	/**
	 * Inits Database.
	 * 
	 * @throws IllegalStateException if {@link BiographyConfig} has been set.
	 */
	public void init() {
		if(this.biographyConfig==null) {
			throw new IllegalStateException("No BiographyConfig set in Database.");
		}
		mongoClient = new MongoClient();
		database = mongoClient.getDatabase(DATABASE_NAME);
	}

	public void close() {
		mongoClient.close();
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
		List<File> mediaFiles = BiographyFileUtils.getMediaFiles(this.biographyConfig.getArchiveFolder());
		mediaFiles.stream().map(fileToDocumentMapper).forEach( document -> {
			collection.insertOne(document);
			addMediaFileToMonthAlbum(albums, document);
			LOG.info(String.format("Inserted %s into database.", document.get("_id")));
		});

		
		LOG.info(String.format("Database rebuilt, containing %d media files.", collection.count()));
	}
	
	private void addMediaFileToMonthAlbum(MongoCollection<Document> albums, Document mediaFile) {
		String dateTimeOriginalAsString = mediaFile.get("dateTimeOriginal", String.class);
		if(dateTimeOriginalAsString==null) {
			return;
		}
		LocalDateTime dateTimeOriginal = LocalDateTime.parse(dateTimeOriginalAsString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		String albumName = String.format("Fotos %s %s", dateTimeOriginal.getMonth(), dateTimeOriginal.getYear());
		FindIterable<Document> findIterable = albums.find(new Document("name", albumName));
		Document album = findIterable.first();
		if(album==null) {
			album = new Document("name", albumName);
			albums.insertOne(album);
		}
		albums.updateOne(new Document("_id", album.get("_id")), new Document("$push", new Document("mediaFiles", mediaFile.get("_id"))));
	}
	
	/**
	 * Refreshes database.
	 */
	public void refresh() {
		// not implemented yet
	}
	
	public void foo() {

		FindIterable<Document> iterable = database.getCollection("biography_files").find();

		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				System.out.println(document.get("_id"));
				System.out.println(document.get("description"));
			}
		});

		Document neuesFoto = new Document("_id", "2016-05-11--12-43-33-423.jpg").append("description", "Weserstadion");
		database.getCollection("biography_files").insertOne(neuesFoto);

	}

	private Function<File, Document> fileToDocumentMapper = (file) -> {
		String sha1 = BiographyFileUtils.sha1(file);
		ExifData exifData = ExifData.from(file);
		Document document = new Document("_id", sha1);
		LocalDateTime dateTimeOriginal = null;
		if(exifData.getDateTimeOriginal()!=null) {
			// TODO das ist das falsche Datum!!!
			dateTimeOriginal = exifData.getDateTimeOriginal();
		} else {
			dateTimeOriginal = LocalDateTime.ofEpochSecond(file.lastModified()/1000, 0, ZoneOffset.UTC);
		}
		document.append("dateTimeOriginal", dateTimeOriginal.toString());
		document.append("fileName", file.getName());
		return document;
	};	
	
}

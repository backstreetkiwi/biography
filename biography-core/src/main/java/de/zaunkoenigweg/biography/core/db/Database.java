package de.zaunkoenigweg.biography.core.db;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.zaunkoenigweg.biography.core.config.BiographyConfig;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

public class Database {

	private static final Logger LOG = LogManager.getLogger(Database.class);

	// TODO Read from Configuration
	private static final String DATABASE_NAME = "biography";
	private static final String COLLECTION_NAME = "media";

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
		return database.getCollection(COLLECTION_NAME).count();
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
		MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
		collection.drop();
		List<File> mediaFiles = BiographyFileUtils.getMediaFiles(this.biographyConfig.getArchiveFolder());
		mediaFiles.stream().map(fileToDocumentMapper).forEach( document -> {
			collection.insertOne(document);
			LOG.info(String.format("Inserted %s into database.", document.get("_id")));
		});
		LOG.info(String.format("Database rebuilt, containing %d media files.", collection.count()));
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
		return new Document("_id", file.getName());
	};	
	
}

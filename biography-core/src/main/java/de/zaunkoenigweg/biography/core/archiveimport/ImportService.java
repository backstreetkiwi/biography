package de.zaunkoenigweg.biography.core.archiveimport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileName;
import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.Sha1;
import de.zaunkoenigweg.biography.core.archive.Archive;
import de.zaunkoenigweg.biography.core.index.IndexingService;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.MetadataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifData;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;
import de.zaunkoenigweg.lexi4j.thumbnails.ThumbnailGenerator;
import de.zaunkoenigweg.lexi4j.thumbnails.ThumbnailGeneratorException;

@Component
public class ImportService {

    private final static Log LOG = LogFactory.getLog(ImportService.class);

    private MetadataService metadataService;
    private IndexingService indexingService;
    private ExifDataService exifDataService;
    private Archive archive;
    private File importFolder;

    public ImportService(MetadataService metadataService, IndexingService indexingService, ExifDataService exifDataService, Archive archive, File importFolder) {
        this.metadataService = metadataService;
        this.indexingService = indexingService;
        this.exifDataService = exifDataService;
        this.archive = archive;
        this.importFolder = importFolder;
        LOG.info("ArchiveImportService started.");
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    /**
     * Imports given media file.
     * @param album
     */
    ImportResult importFile(File file, LocalDateTime dateTimeOriginalFallback, String album, String description) {

        if (!file.exists() || file.isDirectory()) {
            return ImportResult.FILE_NOT_FOUND;
        }

        Optional<MediaFileType> mediaFileType = MediaFileType.of(file);

        if (!mediaFileType.isPresent()) {
            return ImportResult.UNKNOWN_MEDIA_FILE_TYPE;
        }
        
        LocalDateTime dateTimeOriginal = null;

        if(ExifDataService.supports(mediaFileType.get())) {

            ExifData exifData = exifDataService.readExifData(file);

            if (exifData == null) {
                return ImportResult.NO_EXIF_DATA_PRESENT;
            }
            
            dateTimeOriginal = exifData.getDateTimeOriginal();
            
            if (dateTimeOriginal == null) {
                if(dateTimeOriginalFallback==null) {
                    return ImportResult.NO_TIMESTAMP_DETECTED;
                }
                dateTimeOriginal = dateTimeOriginalFallback;
            }
            
        } else {

            if(dateTimeOriginalFallback==null) {
                return ImportResult.NO_EXIF_DATA_SUPPORTED;
            }
            dateTimeOriginal = dateTimeOriginalFallback;
            
        }
        
        Sha1 sha1 = Sha1.calculate(file);

        MediaFileName mediaFileName = MediaFileName.of(mediaFileType.get(), dateTimeOriginal, sha1);        
        
        File archiveFile = mediaFileName.archiveFile(archive.getArchiveFolder());

        if (archiveFile.exists()) {
            return ImportResult.FILE_ALREADY_ARCHIVED;
        }

        try {
            FileUtils.copyFile(file, archiveFile);
        } catch (IOException e) {
            LOG.error("File cannot be stored in archive.", e);
        }

        try {
            setBiographyMetadata(archiveFile, dateTimeOriginal, sha1, album, description);
        } catch (RuntimeException e) {
            LOG.error("Error while setting biography metadata on file " + archiveFile, e);
            FileUtils.deleteQuietly(archiveFile);
            return ImportResult.FILE_CANNOT_BE_STORED;
        }
        
        this.generateThumbnails(archiveFile);
        
        indexingService.reIndex(archiveFile);

        return ImportResult.SUCCESS;
    }

    private void setBiographyMetadata(File file, LocalDateTime dateTimeOriginal, Sha1 sha1,
            String album, String description) {
        MediaFileType mediaFileType = MediaFileType.of(file).get();

        Set<Album> albums = new HashSet<>();
        if (StringUtils.isNotBlank(album)) {
            albums.add(new Album(StringUtils.trim(album)));
        }

        BiographyMetadata metadata = new BiographyMetadata(dateTimeOriginal, sha1, description, albums);

        if (ExifDataService.supports(mediaFileType)) {
            metadataService.writeMetadataIntoExif(file, metadata);
            return;
        }

        File jsonFile = new File(file.getParentFile(),
                "b" + MediaFileName.of(file.getName()).getSha1() + ".json");
        metadataService.writeMetadataToJsonFile(jsonFile, metadata);
    }
    
    /**
     * Generate thumbnail files for given archive file if they don't exist.
     * @param archive file, must have a valid {@code MediaFileName}.
     * @return Have the thumbnails been created?
     */
    public boolean generateThumbnails(File file) {
    	
    	Objects.requireNonNull(file);
    	MediaFileName mediaFileName = MediaFileName.of(file.getName());
    	
    	File thumbnailsFolder = new File(this.archive.getArchiveFolder(), "thumbnails");
    	
    	File thumbsFolder200 = new File(thumbnailsFolder, ThumbnailSize.t200.folderName);
    	File thumbsFolder300 = new File(thumbnailsFolder, ThumbnailSize.t300.folderName);

		File file200 = mediaFileName.thumbnailFile(thumbsFolder200);
		File file300 = mediaFileName.thumbnailFile(thumbsFolder300);

		if(file200.exists() && file300.exists()) {
			return false;
		}
    	
        generateThumbnail(file, ThumbnailSize.t200, file200);
        generateThumbnail(file, ThumbnailSize.t300, file300);
    	
    	return true;
    }

    public void generateImportThumbnails(File file, UUID uuid) {
        File thumbnailsFolder = new File(this.importFolder, "thumbnails");
        File thumbnailFile = new File(thumbnailsFolder, String.format("%s.jpg", uuid.toString()));
        generateThumbnail(file, ThumbnailSize.t200, thumbnailFile);
    }

    private void generateThumbnail(File sourceFile, ThumbnailSize size, File targetFile) {
        
        Optional<MediaFileType> mediaFileType = MediaFileType.of(sourceFile);
        if(!mediaFileType.isPresent()) {
            return;
        }
        
        try {
            Files.createDirectories(targetFile.getParentFile().toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        
        if(mediaFileType.get().getKind() == MediaFileType.Kind.VIDEO) {
        	try {
				ThumbnailGenerator.generateThumbnailFromVideo(sourceFile, targetFile, size.height);
			} catch (ThumbnailGeneratorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return;
        }
        
        if(mediaFileType.get().getKind() == MediaFileType.Kind.IMAGE) {
            try {
                ThumbnailGenerator.generateThumbnailFromImage(sourceFile, targetFile, size.height);
            } catch (ThumbnailGeneratorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
    }
    
    static enum ThumbnailSize {
    	
    	t200("200", 200), 
    	t300("300", 300);
    	
    	private String folderName;
    	private int height;
    	
		private ThumbnailSize(String folderName, int height) {
			this.folderName = folderName;
			this.height = height;
		}
    }

}

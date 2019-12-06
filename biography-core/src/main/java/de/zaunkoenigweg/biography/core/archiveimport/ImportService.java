package de.zaunkoenigweg.biography.core.archiveimport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.index.IndexingService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.MetadataService;
import de.zaunkoenigweg.biography.metadata.exif.ExifDataService;
import de.zaunkoenigweg.lexi4j.thumbnails.ThumbnailGenerator;
import de.zaunkoenigweg.lexi4j.thumbnails.ThumbnailGeneratorException;
import de.zaunkoenigweg.biography.metadata.exif.ExifData;

@SuppressWarnings("deprecation")
@Component
public class ImportService {

    private final static Log LOG = LogFactory.getLog(ImportService.class);

    private MetadataService metadataService;
    private IndexingService indexingService;
    private ExifDataService exifDataService;
    private File archiveFolder;
    private File importFolder;
    private String thumborUrl;

    public ImportService(MetadataService metadataService, IndexingService indexingService, ExifDataService exifDataService, File archiveFolder, File importFolder, String thumborUrl) {
        this.metadataService = metadataService;
        this.indexingService = indexingService;
        this.exifDataService = exifDataService;
        this.archiveFolder = archiveFolder;
        this.importFolder = importFolder;
        this.thumborUrl = thumborUrl;
        LOG.info("ArchiveImportService started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
        LOG.info(String.format("importFolder=%s", this.importFolder));
    }

    /**
     * Imports given media file.
     * @param album
     */
    public ImportResult importFile(File file, LocalDateTime dateTimeOriginalFallback, String album, String description) {

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
        
        String sha1 = BiographyFileUtils.sha1(file);
        
        File archiveFile = BiographyFileUtils.buildArchiveFilename(archiveFolder, file,
                dateTimeOriginal, mediaFileType.get(), sha1).toFile();

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

    private void setBiographyMetadata(File file, LocalDateTime dateTimeOriginal, String sha1,
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
                "b" + BiographyFileUtils.getSha1FromArchiveFilename(file) + ".json");
        metadataService.writeMetadataToJsonFile(jsonFile, metadata);
    }
    
    public boolean generateThumbnails(File file) {
    	
    	File thumbnailsFolder = new File(this.archiveFolder, "thumbnails");
    	
    	File thumbsFolder200 = new File(thumbnailsFolder, ThumbnailSize.t200.folderName);
    	File thumbsFolder300 = new File(thumbnailsFolder, ThumbnailSize.t300.folderName);

    	String thumbnailFileName = StringUtils.substringBeforeLast(file.getName(), ".") + ".jpg";
    	
		File file200 = BiographyFileUtils.getArchiveFileFromShortFilename(thumbsFolder200, thumbnailFileName);
		File file300 = BiographyFileUtils.getArchiveFileFromShortFilename(thumbsFolder300, thumbnailFileName);

		if(file200.exists() && file300.exists()) {
			return false;
		}
    	
        generateThumbnail(file, this.archiveFolder, "archive/", ThumbnailSize.t200, file200);
        generateThumbnail(file, this.archiveFolder, "archive/", ThumbnailSize.t300, file300);
    	
    	return true;
    }

    public void generateImportThumbnails(File file, UUID uuid) {
        File thumbnailsFolder = new File(this.importFolder, "thumbnails");
        File thumbnailFile = new File(thumbnailsFolder, String.format("%s.jpg", uuid.toString()));
        generateThumbnail(file, this.importFolder, "import/", ThumbnailSize.t200, thumbnailFile);
    }

    private void generateThumbnail(File sourceFile, File sourceBaseFolder, String thumborBaseFolder, ThumbnailSize size, File targetFile) {
        
        Optional<MediaFileType> mediaFileType = MediaFileType.of(sourceFile);
        if(!mediaFileType.isPresent()) {
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
        
		try (DefaultHttpClient client = new DefaultHttpClient()) {

            String thumborUri = this.thumborUrl + size.thumborUrlFragment + thumborBaseFolder + sourceBaseFolder.toPath().relativize(sourceFile.toPath()).toString();
            HttpGet request = new HttpGet(thumborUri);
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode()==200) {
                BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
                targetFile.getParentFile().mkdirs();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                int inByte;
                while((inByte = bis.read()) != -1) bos.write(inByte);
                bis.close();
                bos.close();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    static enum ThumbnailSize {
    	
    	t200("200", "0x200/", 200), 
    	t300("300", "0x300/", 300);
    	
    	private String folderName;
    	private String thumborUrlFragment;
    	private int height;
    	
		private ThumbnailSize(String folderName, String thumborUrlFragment, int height) {
			this.folderName = folderName;
			this.thumborUrlFragment = thumborUrlFragment;
			this.height = height;
		}
    }

}

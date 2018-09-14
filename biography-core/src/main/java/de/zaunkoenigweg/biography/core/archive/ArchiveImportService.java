package de.zaunkoenigweg.biography.core.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.index.ArchiveIndexingService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.metadata.MetadataService;

@Component
public class ArchiveImportService {

    private final static Log LOG = LogFactory.getLog(ArchiveImportService.class);

    private MetadataService metadataService;
    private ArchiveIndexingService archiveIndexingService;
    private File archiveFolder;
    private String thumborUrl;

    public ArchiveImportService(MetadataService metadataService, ArchiveIndexingService archiveIndexingService, File archiveFolder, String thumborUrl) {
        this.metadataService = metadataService;
        this.archiveIndexingService = archiveIndexingService;
        this.archiveFolder = archiveFolder;
        this.thumborUrl = thumborUrl;
        LOG.info("ArchiveImportService started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
    }

    /**
     * Imports given media file.
     * @param album
     */
    public ImportResult importFile(File file, boolean readLegacyDescription, LocalDateTime dateTimeOriginalFallback, String album) {

        if (!file.exists() || file.isDirectory()) {
            return ImportResult.FILE_NOT_FOUND;
        }

        Optional<MediaFileType> mediaFileType = MediaFileType.of(file);

        if (!mediaFileType.isPresent()) {
            return ImportResult.UNKNOWN_MEDIA_FILE_TYPE;
        }
        
        LocalDateTime dateTimeOriginal = null;

        if(ExifData.supports(mediaFileType.get())) {

            ExifData exifData = ExifData.of(file);

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

        setBiographyMetadata(archiveFile, dateTimeOriginal, sha1, readLegacyDescription, album);
        
        archiveIndexingService.reIndex(archiveFile);

        return ImportResult.SUCCESS;
    }

    private void setBiographyMetadata(File file, LocalDateTime dateTimeOriginal, String sha1, boolean readLegacyDescription,
            String album) {
        MediaFileType mediaFileType = MediaFileType.of(file).get();
        String description = null;
        if (readLegacyDescription && ExifData.supports(mediaFileType)) {
            description = ExifData.of(file, StandardCharsets.ISO_8859_1).getDescription().orElse(null);
        }

        Set<Album> albums = new HashSet<>();
        if (StringUtils.isNotBlank(album)) {
            albums.add(new Album(StringUtils.trim(album)));
        }

        BiographyMetadata metadata = new BiographyMetadata(dateTimeOriginal, sha1, description, albums);

        if (ExifData.supports(mediaFileType)) {
            metadataService.writeMetadataIntoExif(file, metadata);
            return;
        }

        File jsonFile = new File(file.getParentFile(),
                "b" + BiographyFileUtils.getSha1FromArchiveFilename(file) + ".json");
        metadataService.writeMetadataToJsonFile(jsonFile, metadata);
    }
    
    public boolean generateThumbnails(File file, boolean force) {
    	
    	File thumbnailsFolder = new File(this.archiveFolder, "thumbnails");
    	
    	File thumbsFolder200 = new File(thumbnailsFolder, "200");
    	File thumbsFolder300 = new File(thumbnailsFolder, "300");
    	HttpClient client;

    	try {
			client = new DefaultHttpClient();

			// TODO method for one resizing w/param (200, 300, ...)

			File file200 = BiographyFileUtils.getArchiveFileFromShortFilename(thumbsFolder200, file.getName());
			File file300 = BiographyFileUtils.getArchiveFileFromShortFilename(thumbsFolder300, file.getName());

			if(!force && (file200.exists() && file300.exists())) {
				return true;
			}
			
			String thumborUri = this.thumborUrl + "0x200/" + archiveFolder.toPath().relativize(file.toPath()).toString();
			HttpGet request = new HttpGet(thumborUri);
			HttpResponse response = client.execute(request);
			if(response.getStatusLine().getStatusCode()==200) {
				BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
				file200.getParentFile().mkdirs();
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file200));
				int inByte;
				while((inByte = bis.read()) != -1) bos.write(inByte);
				bis.close();
				bos.close();
			}

			thumborUri = this.thumborUrl + "0x300/" + archiveFolder.toPath().relativize(file.toPath()).toString();
			request = new HttpGet(thumborUri);
			response = client.execute(request);
			if(response.getStatusLine().getStatusCode()==200) {
				BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
				file300.getParentFile().mkdirs();
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file300));
				int inByte;
				while((inByte = bis.read()) != -1) bos.write(inByte);
				bis.close();
				bos.close();
				return true;
			}
			return false;
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			// TODO 
			// close client ?
		}
    }
}

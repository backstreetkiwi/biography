package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.lexi4j.exiftool.ExifData;
import de.zaunkoenigweg.lexi4j.exiftool.Exiftool;

@Component
public class ExifDataService {

    private final static Log LOG = LogFactory.getLog(ExifDataService.class);

    private Exiftool exiftool;
    
    private File archiveFolder;

    public ExifDataService(File archiveFolder) {
        this.exiftool = new Exiftool();
        this.archiveFolder = archiveFolder;
        LOG.info("ExifDataService started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}
    
    public ExifDataWrapper getExifData(File file) {
        ExifDataWrapper exifData;
        Optional<ExifData> rawExifData;
        try {
            rawExifData = this.exiftool.read(file);
            if(!rawExifData.isPresent()) {
                return null;
            }
            exifData = new ExifDataWrapper(rawExifData.get());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
        return exifData;
    }
    
    public ExifDataWrapper setExifData(File file, ExifDataWrapper newExifData) {
        this.exiftool.update(file)
            .withDateTimeOriginal(newExifData.getDateTimeOriginal().truncatedTo(ChronoUnit.SECONDS))
            .withSubsecTimeOriginal(newExifData.getDateTimeOriginal().getNano() / 1_000_000)
            .withImageDescription(newExifData.getDescription().orElse(""))
            .withUserComment(newExifData.getUserComment().orElse(""))
            .perform();
        
        return newExifData;
    }

    public void fillCacheFromArchive(Consumer<String> console) {
        BiographyFileUtils.getMediaFolders(this.archiveFolder).stream().forEach(mediaFolder -> {
            this.exiftool.fillCache(mediaFolder + "/*.jpg");
            console.accept(String.format("Cached EXIF data from %s", mediaFolder));
        });
        
    }
    
    /**
     * Does this service support the given media file type?
     * 
     * @param mediaFileType media file type
     * @return Does this service support the given media file type?
     */
    public static boolean supports(MediaFileType mediaFileType) {
      return MediaFileType.JPEG == mediaFileType;
    }

}

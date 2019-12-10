package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.archive.Archive;
import de.zaunkoenigweg.lexi4j.exiftool.Exiftool;

/**
 * Read/Write EXIF data.
 * 
 * This service wraps the {@link de.zaunkoenigweg.lexi4j.exiftool.Exiftool}.
 */
@Component
public class ExifDataService {

    private final static Log LOG = LogFactory.getLog(ExifDataService.class);

    /**
     * Lexi4J Exiftool
     */
    private Exiftool exiftool;

    /**
     * Biography archive
     */
    private Archive archive;

    public ExifDataService(Archive archive) {
        this.exiftool = new Exiftool();
        this.archive = archive;
        LOG.info("ExifDataService started.");
	}
    
    /**
     * Read EXIF data from the given file
     * @param media file
     * @return EXIF Data, {@code null} if data cannot be read.
     * TODO consider Optional
     */
    public ExifData readExifData(File file) {
        ExifData exifData;
        Optional<de.zaunkoenigweg.lexi4j.exiftool.ExifData> rawExifData;
        try {
            rawExifData = this.exiftool.read(file);
            if(!rawExifData.isPresent()) {
                return null;
            }
            exifData = new ExifData(rawExifData.get());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
        return exifData;
    }
    
    /**
     * Write EXIF data to given file
     * @param file media file
     * @param newExifData EXIF data to write
     */
    public void writeExifData(File file, ExifData newExifData) {
        this.exiftool.update(file)
            .withDateTimeOriginal(newExifData.getDateTimeOriginal().truncatedTo(ChronoUnit.SECONDS))
            .withSubsecTimeOriginal(newExifData.getDateTimeOriginal().getNano() / 1_000_000)
            .withImageDescription(newExifData.getDescription().orElse(""))
            .withUserComment(newExifData.getUserComment().orElse(""))
            .perform();
    }

    /**
     * Fills the cache of the Exiftool
     * @param console console output stream
     */
    public void fillCacheFromArchive(Consumer<String> console) {
        archive.mediaFolders().stream().forEach(mediaFolder -> {
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

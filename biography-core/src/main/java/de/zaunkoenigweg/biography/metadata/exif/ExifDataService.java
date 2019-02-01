package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.lexi4j.exiftool.ExifData;
import de.zaunkoenigweg.lexi4j.exiftool.Exiftool;

@Component
public class ExifDataService {

    private final static Log LOG = LogFactory.getLog(ExifDataService.class);

    private Map<File, ExifDataWrapper> cache = new HashMap<>();
    
    public ExifDataService() {
        LOG.info("ExifDataService started.");
	}
    
    public ExifDataWrapper getExifData(File file) {
        ExifDataWrapper exifData;
        if(cache.containsKey(file)) {
            return cache.get(file);
        }
        Optional<ExifData> rawExifData;
        try {
            rawExifData = Exiftool.read(file);
            if(!rawExifData.isPresent()) {
                return null;
            }
            exifData = new ExifDataWrapper(rawExifData.get());
            cache.put(file, exifData);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
        return exifData;
    }
    
    public ExifDataWrapper setExifData(File file, ExifDataWrapper newExifData) {
        Exiftool.update(file)
            .withDateTimeOriginal(newExifData.getDateTimeOriginal().truncatedTo(ChronoUnit.SECONDS))
            .withImageDescription(newExifData.getDescription().orElse(""))
            .withUserComment(newExifData.getUserComment().orElse(""))
            .perform();
        
        cache.put(file, newExifData);
        
        return newExifData;
    }

    public void fillCacheFromArchive(String path) {
        Map<File, ExifData> exifData = Exiftool.readPaths(path);
        exifData.entrySet().stream().forEach(entry -> {
            LOG.info(String.format("Reading EXIF Data for %s into cache...", entry.getKey().getAbsolutePath()));
            cache.put(entry.getKey(), new ExifDataWrapper(entry.getValue()));
        });
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public int getCacheSize() {
        return cache.size();
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

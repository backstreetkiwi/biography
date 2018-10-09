package de.zaunkoenigweg.biography.metadata.exif;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import de.zaunkoenigweg.biography.core.MediaFileType;

@Component
public class ExifDataService {

    private final static Log LOG = LogFactory.getLog(ExifDataService.class);

    private Map<File, ExifData> cache = new HashMap<>();
    
    public ExifDataService() {
        LOG.info("ExifDataService started.");
	}
    
    public ExifData getExifData(File file) {
        if(cache.containsKey(file)) {
            return cache.get(file);
        }
        Map<Exif, String> rawExifData;
        try {
            rawExifData = Exiftool.read(file, ExifData.EXIF_FIELDS);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
        ExifData exifData = new ExifData(rawExifData);
        cache.put(file, exifData);
        return exifData;
    }
    
    public ExifData setExifData(File file, ExifData newExifData) {
        Map<Exif, String> values = new HashMap<>();
        values.putAll(newExifData.getDateTimeOriginalExifFields());
        values.put(Exif.IMAGE_DESCRIPTION, newExifData.getDescription().orElse(""));
        values.put(Exif.CAMERA_MODEL, newExifData.getCameraModel().orElse(""));
        values.put(Exif.USER_COMMENT, newExifData.getUserComment().orElse(""));
        Exiftool.write(file, values);

        cache.put(file, newExifData);
        
        return newExifData;
    }

    public void fillCacheFromArchive(String path) {
        Map<File, Map<Exif, String>> exifData = Exiftool.readPaths(path, ExifData.EXIF_FIELDS);
        exifData.entrySet().stream().forEach(entry -> {
            cache.put(entry.getKey(), new ExifData(entry.getValue()));
        });
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * Does this class support the given media file type?
     * 
     * @param mediaFileType
     *          media file type
     * @return
     */
    public static boolean supports(MediaFileType mediaFileType) {
      return MediaFileType.JPEG == mediaFileType;
    }

    

    // TODO replace ExifDataOld w/ ExifData POJO
    
}

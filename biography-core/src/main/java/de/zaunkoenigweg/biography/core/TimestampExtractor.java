package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.ExifData;

@FunctionalInterface
public interface TimestampExtractor extends Function<File, LocalDateTime>{
    
    public final static TimestampExtractor FROM_EXIF = file -> {
        ExifData exifData = ExifData.of(file);
        if(exifData==null) {
            return null;
        }
        return exifData.getDateTimeOriginal();
    };
    
    public final static TimestampExtractor FROM_FILE_LAST_MODIFIED = file -> {
        return LocalDateTime.ofEpochSecond(file.lastModified() / 1000, 0, ZoneOffset.UTC);
    };
    
    public final static TimestampExtractor FROM_ARCHIVE_FILENAME = file -> {
        return BiographyFileUtils.getDatetimeOriginalFromArchiveFilename(file);
    };
    
    public final static TimestampExtractor FROM_EXIF_OR_ELSE_ARCHIVE_FILENAME = FROM_EXIF.orElse(FROM_ARCHIVE_FILENAME);
    public final static TimestampExtractor FROM_EXIF_OR_ELSE_FILE_LAST_MODIFIED = FROM_EXIF.orElse(FROM_FILE_LAST_MODIFIED);
    
    default public TimestampExtractor orElse(TimestampExtractor alternativeTimestampExtractor) {
        return file -> {
            LocalDateTime thisApplied = this.apply(file);
            if(thisApplied!=null) {
                return thisApplied;
            }
            System.out.printf("TODO: Logging: Using alternative timestamp extractor for %s.%n", file.getName());
            return alternativeTimestampExtractor.apply(file);
        };
    }

}

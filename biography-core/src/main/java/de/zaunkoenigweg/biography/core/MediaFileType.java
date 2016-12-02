package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public enum MediaFileType {
    
    JPEG("jpg", TimestampExtractor.FROM_EXIF_OR_ELSE_FILE_LAST_MODIFIED, TimestampExtractor.FROM_EXIF_OR_ELSE_ARCHIVE_FILENAME),
    QUICKTIME("mov", TimestampExtractor.FROM_FILE_LAST_MODIFIED, TimestampExtractor.FROM_ARCHIVE_FILENAME);
    
    private String fileExtension;
    private TimestampExtractor timestampExtractorForUntrackedFiles;
    private TimestampExtractor timestampExtractorForArchivedFiles;

    private MediaFileType(String fileExtension, TimestampExtractor timestampExtractorForUntrackedFiles, TimestampExtractor timestampExtractorForArchivedFiles) {
        this.fileExtension = fileExtension;
        this.timestampExtractorForUntrackedFiles = timestampExtractorForUntrackedFiles;
        this.timestampExtractorForArchivedFiles = timestampExtractorForArchivedFiles;
    }

    public static Stream<MediaFileType> all() {
        return Stream.of(values());
    }
    
    public static Optional<MediaFileType> of(final File file) {
        return all().filter(fileType -> fileType.isTypeOf(file)).findFirst();
    }
    
    public boolean isTypeOf(File file) {
        return StringUtils.endsWithIgnoreCase(file.getAbsolutePath(), "." + fileExtension);
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public TimestampExtractor getTimestampExtractorForUntrackedFiles() {
        return timestampExtractorForUntrackedFiles;
    }

    public TimestampExtractor getTimestampExtractorForArchivedFiles() {
        return timestampExtractorForArchivedFiles;
    }
    
}

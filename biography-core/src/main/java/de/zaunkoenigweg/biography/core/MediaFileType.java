package de.zaunkoenigweg.biography.core;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public enum MediaFileType {
    
    JPEG("jpg"),
    QUICKTIME("mov"),
    AVI("avi"),
    MPEG("mpg");
    
    private String fileExtension;

    private MediaFileType(String fileExtension) {
        this.fileExtension = fileExtension;
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
}

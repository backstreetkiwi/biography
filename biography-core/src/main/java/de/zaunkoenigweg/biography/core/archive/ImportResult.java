package de.zaunkoenigweg.biography.core.archive;

public enum ImportResult {
    FILE_NOT_FOUND,
    UNKNOWN_MEDIA_FILE_TYPE,
    NO_EXIF_DATA_SUPPORTED(true),
    NO_EXIF_DATA_PRESENT,
    NO_TIMESTAMP_DETECTED(true),
    FILE_ALREADY_ARCHIVED,
    FILE_CANNOT_BE_STORED,
    SUCCESS;
    
    private boolean dateTimeOriginalRequired = false;

    private ImportResult() {
    }
    
    private ImportResult(boolean dateTimeOriginalRequired) {
        this.dateTimeOriginalRequired = dateTimeOriginalRequired;
    }
    
    @Override
    public String toString() {
        return super.toString() + (this.dateTimeOriginalRequired ? " (requires dateTimeOriginal)" : "");
    }
    
}
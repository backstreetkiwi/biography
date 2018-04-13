package de.zaunkoenigweg.biography.core.archive;

/**
 * All outcomes of the import of a single file.
 * 
 * @author mail@nikolaus-winter.de
 */
public enum ImportResult {
    FILE_NOT_FOUND,
    UNKNOWN_MEDIA_FILE_TYPE,
    NO_EXIF_DATA_SUPPORTED,
    NO_EXIF_DATA_PRESENT,
    NO_TIMESTAMP_DETECTED,
    FILE_ALREADY_ARCHIVED,
    FILE_CANNOT_BE_STORED,
    SUCCESS;
}
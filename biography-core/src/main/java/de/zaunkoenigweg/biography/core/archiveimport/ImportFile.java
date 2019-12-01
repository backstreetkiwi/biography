package de.zaunkoenigweg.biography.core.archiveimport;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import de.zaunkoenigweg.biography.core.MediaFileType;

/**
 * Holds the data of a media file to be imported (aka 'staged media file')
 * 
 * @author mail@nikolaus-winter.de
 */
public class ImportFile {

    private UUID uuid;
    private String originalFileName;
    private MediaFileType mediaFileType;
    private LocalDateTime datetimeOriginal;
    private String description;
    private String album;
    private ImportResult importResult;
    
    // TODO assert not null
    public ImportFile(UUID uuid, String originalFileName, MediaFileType mediaFileType) {
        this.uuid = uuid;
        this.originalFileName = originalFileName;
        this.mediaFileType = mediaFileType;
    }

    public boolean isReadyForImport() {
        return this.datetimeOriginal != null;
    }
    
    public UUID getUuid() {
        return uuid;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public MediaFileType getMediaFileType() {
        return mediaFileType;
    }
    
    public Optional<LocalDateTime> getDatetimeOriginal() {
        return Optional.ofNullable(datetimeOriginal);
    }

    public void setDatetimeOriginal(LocalDateTime datetimeOriginal) {
        this.datetimeOriginal = datetimeOriginal;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Optional<String> getAlbum() {
        return Optional.ofNullable(album);
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public ImportResult getImportResult() {
        return importResult;
    }

    public void setImportResult(ImportResult importResult) {
        this.importResult = importResult;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImportFile other = (ImportFile) obj;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }
}

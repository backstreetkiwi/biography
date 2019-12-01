package de.zaunkoenigweg.biography.core.archiveimport;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Holds the data of a bulk import job.
 * 
 * ! The {@link ImportFile}s stored in this object are handed to the client and may be altered! That's fine as of now!
 * 
 * @author mail@nikolaus-winter.de
 */
public class BulkImportJob {
    
    private boolean running;
    private Map<UUID,ImportFile> importFiles = new HashMap<>();
    
    public List<ImportFile> getImportFiles() {
        return this.importFiles.values().stream()
                        .sorted(Comparator.comparing(ImportFile::getOriginalFileName))
                        .collect(Collectors.toList());
    }
    
    public void put(ImportFile importFile) {
        synchronized (importFiles) {
            importFiles.put(importFile.getUuid(), importFile);
        }
    }
    
    public Optional<ImportFile> get(UUID uuid) {
        return Optional.ofNullable(importFiles.get(uuid));
    }
    
    public void remove(Collection<UUID> uuid) {
        synchronized (importFiles) {
            uuid.forEach(importFiles::remove);
        }
    }
    
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

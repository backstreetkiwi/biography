package de.zaunkoenigweg.biography.core.importer;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ImportLog {

    private final Map<File, Path> imported = new HashMap<>();
    private final Map<File, String> notImported = new HashMap<>();
    
    public void imported(File file, Path archivePath) {
        this.imported.put(file, archivePath);
    }
    
    public void notImported(File file, String message) {
        this.notImported.put(file, message);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("# imported: %d%n", this.imported.size()));
        builder.append(String.format("# not imported: %d%n", this.notImported.size()));
        imported.keySet().stream().sorted().forEach((file)-> {
            builder.append(String.format("imported: %s -> %s%n", file.getName(), imported.get(file)));
        });
        notImported.forEach((file,message)-> {
            builder.append(String.format("not imported: %s -> %s%n", file.getName(), message));
        });
        return builder.toString();
    }
    
}

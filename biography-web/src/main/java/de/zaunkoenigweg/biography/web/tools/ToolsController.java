package de.zaunkoenigweg.biography.web.tools;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.metadata.ExifData;
import de.zaunkoenigweg.biography.web.console.Console;
import de.zaunkoenigweg.biography.web.console.Consoles;

@Controller
public class ToolsController {

	private final static Log LOG = LogFactory.getLog(ToolsController.class);

	private File archiveFolder;

	private ArchiveValidationService archiveValidationService;

	private ArchiveMetadataService archiveMetadataService;
	
	private Consoles consoles;

	public ToolsController(File archiveFolder, ArchiveValidationService archiveValidationService, ArchiveMetadataService archiveMetadataService,
			Consoles consoles) {
		this.archiveFolder = archiveFolder;
		this.archiveValidationService = archiveValidationService;
		this.archiveMetadataService = archiveMetadataService;
		this.consoles = consoles;
		LOG.info("ToolsController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/tools")
	public String statistics(Model model) {
		return "tools/index";
	}

	@RequestMapping("/tools/inspect-archive")
	public String statisticsInspectArchive(Model model) {

		Console console = consoles.create("inspect archive");

		new Thread(() -> {
			List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

			int totalNumberOfFiles = mediaFiles.size();
			AtomicInteger numberOfCorruptFiles = new AtomicInteger(0);

			mediaFiles.stream().forEach(file -> {

				Pair<Boolean, List<Pair<String, Boolean>>> check = archiveValidationService.check(file);
				if (check.getLeft()) {
					console.println(String.format("File '%s' -> [OK]", file.getAbsolutePath()));
				} else {
					numberOfCorruptFiles.incrementAndGet();
					;
					console.println(String.format("ERROR in file '%s'", file.getAbsolutePath()));
					check.getRight().stream().forEach(pair -> {
						console.println(String.format("%-100s [%s]", pair.getLeft(), pair.getRight() ? "OK" : "ERROR"));
					});
	                console.println("");
				}

			});

			console.println(String.format("%n%nValidated files #: %d, # of corrupt files: %d%n", totalNumberOfFiles,
					numberOfCorruptFiles.get()));
			console.close();
		}).start();
		

		return "redirect:/console";

	}
	
	@RequestMapping("/tools/inspect-file/{file}")
	public String dumpFileDetails(Model model, @PathVariable("file")String filename) {

		Console console = consoles.create("inspect-file " + filename);

		new Thread(() -> {
			File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
			
            Pair<Boolean, List<Pair<String, Boolean>>> check = archiveValidationService.check(archiveFile);
            if (!check.getLeft()) {
                console.println(String.format("ERROR in file '%s'", archiveFile.getAbsolutePath()));
                check.getRight().stream().forEach(pair -> {
                    console.println(String.format("%-100s [%s]", pair.getLeft(), pair.getRight() ? "OK" : "ERROR"));
                });
                console.println("");
                console.close();
                return;
            }
            
            console.println(String.format("File '%s' -> [OK]%n", archiveFile.getAbsolutePath()));

            BiographyMetadata metadata = archiveMetadataService.getMetadata(archiveFile);
			
			console.println(String.format("Filename: %s", archiveFile));
			console.println(String.format("DateTimeOriginal: %s", metadata.getDateTimeOriginal()));
			console.println(String.format("Description: %s", metadata.getDescription()));
			console.println(String.format("Albums: %s", metadata.getAlbums()));

			console.println("");
			console.println(ExifData.dumpExif(archiveFile));

            console.close();
            
		}).start();

		return "redirect:/console";
	}


}
package de.zaunkoenigweg.biography.web;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.archive.ArchiveValidationService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.web.console.Console;
import de.zaunkoenigweg.biography.web.console.Consoles;

@Controller
public class StatisticsController {

	private final static Log LOG = LogFactory.getLog(StatisticsController.class);

	private File archiveFolder;

	private ArchiveValidationService archiveValidationService;

	private Consoles consoles;

	public StatisticsController(File archiveFolder, ArchiveValidationService archiveValidationService,
			Consoles consoles) {
		this.archiveFolder = archiveFolder;
		this.archiveValidationService = archiveValidationService;
		this.consoles = consoles;
		LOG.info("StatisticsController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/statistics")
	public String statistics(Model model) {
		long count = BiographyFileUtils.getMediaFiles(archiveFolder).stream().count();
		model.addAttribute("count", count);

		return "statistics";
	}

	@RequestMapping("/statistics/inspect-archive")
	public String statisticsInspectArchive(Model model) {

		Console console = consoles.create("inspect archive");

		new Thread(() -> {
			List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

			int totalNumberOfFiles = mediaFiles.size();
			AtomicInteger numberOfCorruptFiles = new AtomicInteger(0);

			mediaFiles.stream().limit(10).forEach(file -> {

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
				}

			});

			console.println(String.format("%n%nValidated files #: %d, # of corrupt files: %d%n", totalNumberOfFiles,
					numberOfCorruptFiles.get()));
			console.close();
		}).start();
		

		return "redirect:/console";

	}

}
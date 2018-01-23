package de.zaunkoenigweg.biography.web;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
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

@Controller
public class StatisticsController {

	private final static Log LOG = LogFactory.getLog(StatisticsController.class);

	private File archiveFolder;

	private ArchiveValidationService archiveValidationService;

	private Console console;

	public StatisticsController(File archiveFolder, ArchiveValidationService archiveValidationService,
			Console console) {
		this.archiveFolder = archiveFolder;
		this.archiveValidationService = archiveValidationService;
		this.console = console;
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

		Writer writer = console.writer();

		new Thread(() -> {
			List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);

			PrintWriter pw = new PrintWriter(writer);
			int totalNumberOfFiles = mediaFiles.size();
			AtomicInteger numberOfCorruptFiles = new AtomicInteger(0);

			mediaFiles.stream().limit(20).forEach(file -> {

				Pair<Boolean, List<Pair<String, Boolean>>> check = archiveValidationService.check(file);
				if (check.getLeft()) {
					pw.printf("File '%s' -> [OK]%n", file.getAbsolutePath());
				} else {
					numberOfCorruptFiles.incrementAndGet();
					;
					pw.printf("ERROR in file '%s'%n", file.getAbsolutePath());
					check.getRight().stream().forEach(pair -> {
						printMessage(pw, pair.getLeft(), pair.getRight());
					});
				}

				pw.flush();
			});

			pw.printf("%n%nValidated files #: %d, # of corrupt files: %d%n%n", totalNumberOfFiles,
					numberOfCorruptFiles.get());
			pw.flush();
		}).start();

		return "redirect:/console";

	}

	private static void printMessage(PrintWriter pw, String message, boolean okay) {
		pw.printf("%-100s [%s]%n", message, okay ? "OK" : "ERROR");
	}

}
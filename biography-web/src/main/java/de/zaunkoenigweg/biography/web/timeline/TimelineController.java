package de.zaunkoenigweg.biography.web.timeline;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

@Controller
public class TimelineController {

	private final static Log LOG = LogFactory.getLog(TimelineController.class);

	private File archiveFolder;

	public TimelineController(File archiveFolder) {
		this.archiveFolder = archiveFolder;
		LOG.info("TimelineController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/timeline")
	public String timeline(Model model) {

		List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);
		
		List<String> list = mediaFiles.stream().map(File::getName).collect(Collectors.toList());
		
		BiographyFileUtils.getDatetimeOriginalFromArchiveFilename(mediaFiles.stream().findFirst().get());
		
		model.addAttribute("list", list);
		
		return "timeline/index";
	}
}
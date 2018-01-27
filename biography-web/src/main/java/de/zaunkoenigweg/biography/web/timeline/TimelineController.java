package de.zaunkoenigweg.biography.web.timeline;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
		return "timeline/index";
	}
}
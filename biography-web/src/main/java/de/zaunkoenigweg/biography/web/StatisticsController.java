package de.zaunkoenigweg.biography.web;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

@Controller
public class StatisticsController {

	private final static Log LOG = LogFactory.getLog(StatisticsController.class);

	private File archiveFolder;

    public StatisticsController(File archiveFolder) {
		this.archiveFolder = archiveFolder;
		LOG.info("StatisticsController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/statistics")
    public String statistics(Model model) {
		long count = BiographyFileUtils.getMediaFiles(archiveFolder).stream().count();
		model.addAttribute("count", count);
        return "statistics";
    }

}
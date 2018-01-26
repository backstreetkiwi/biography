package de.zaunkoenigweg.biography.web;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

@Controller
public class MainController {

	private final static Log LOG = LogFactory.getLog(MainController.class);

	private File archiveFolder;

	public MainController(File archiveFolder) {
		this.archiveFolder = archiveFolder;
		LOG.info("MainController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/")
    public String greeting(Model model) {
		long mediaFileCount = BiographyFileUtils.getMediaFiles(archiveFolder).stream().count();
		model.addAttribute("mediaFileCount", mediaFileCount);
        return "start";
        
    }

}
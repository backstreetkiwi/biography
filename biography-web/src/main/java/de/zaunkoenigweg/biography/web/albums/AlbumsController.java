package de.zaunkoenigweg.biography.web.albums;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AlbumsController {

	private final static Log LOG = LogFactory.getLog(AlbumsController.class);

	private File archiveFolder;

	public AlbumsController(File archiveFolder) {
		this.archiveFolder = archiveFolder;
		LOG.info("AlbumsController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/albums")
	public String albums(Model model) {
		return "albums/index";
	}
}
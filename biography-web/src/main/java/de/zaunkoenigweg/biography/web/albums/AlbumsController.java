package de.zaunkoenigweg.biography.web.albums;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.index.Album;
import de.zaunkoenigweg.biography.core.index.ArchiveSearchService;
import de.zaunkoenigweg.biography.core.index.MediaFile;
import de.zaunkoenigweg.biography.web.BackLink;

@Controller
public class AlbumsController {

	private final static Log LOG = LogFactory.getLog(AlbumsController.class);

	private File archiveFolder;

	private ArchiveSearchService archiveSearchService;
	
	public AlbumsController(File archiveFolder, ArchiveSearchService archiveSearchService) {
		this.archiveFolder = archiveFolder;
        this.archiveSearchService = archiveSearchService;
		LOG.info("AlbumsController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}
	
    @RequestMapping("/albums")
    public String albums(Model model) {
        
        List<Album> albums = archiveSearchService.getAlbumCounts().collect(Collectors.toList());
        
        model.addAttribute("albums", albums);
        model.addAttribute("selectedMenuItem", "ALBUMS");
        
        return "albums/index";
    }
    
    @RequestMapping("/album/{album}")
    public String album(HttpSession session, HttpServletRequest request, Model model, @PathVariable("album") String album) {
        
        List<Album> albums = archiveSearchService.getAlbumCounts().collect(Collectors.toList());
        
        List<MediaFile> mediaFiles = archiveSearchService.findByAlbum(album).collect(Collectors.toList());

        BackLink backLink = new BackLink("BACK TO ALBUM", request.getRequestURI());
		session.setAttribute(BackLink.class.getName(), backLink);
        
        model.addAttribute("albums", albums);
        model.addAttribute("mediaFiles", mediaFiles);
        model.addAttribute("selectedMenuItem", "ALBUMS");
        
        return "albums/index";
    }
    
}
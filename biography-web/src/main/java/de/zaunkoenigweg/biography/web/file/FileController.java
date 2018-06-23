package de.zaunkoenigweg.biography.web.file;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.index.ArchiveIndexingService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.web.BackLink;

@Controller
public class FileController {

	private final static Log LOG = LogFactory.getLog(FileController.class);

	private File archiveFolder;
	
	private ArchiveMetadataService archiveMetadataService;

	private ArchiveIndexingService archiveIndexingService;

	public FileController(File archiveFolder, ArchiveMetadataService archiveMetadataService, ArchiveIndexingService archiveIndexingService) {
		this.archiveFolder = archiveFolder;
		this.archiveMetadataService = archiveMetadataService;
		this.archiveIndexingService = archiveIndexingService;
		LOG.info("FileController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

    @RequestMapping("/file/{file}")
    public String fileDetails(HttpSession session, Model model, @PathVariable("file")String filename) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        BiographyMetadata metadata = archiveMetadataService.getMetadata(archiveFile);

        model.addAttribute("backLink", getBackLink(session));
        
        model.addAttribute("dateTimeOriginal", metadata.getDateTimeOriginal());
        model.addAttribute("albums", metadata.getAlbums());
        model.addAttribute("description", metadata.getDescription());
        model.addAttribute("fileName", filename);
        model.addAttribute("editMode", false);
        
        return "file/index";
    }
    
    @GetMapping("/file/{file}/edit")
    public String editFile(HttpSession session, Model model, @PathVariable("file")String filename) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        BiographyMetadata metadata = archiveMetadataService.getMetadata(archiveFile);
        
        model.addAttribute("backLink", getBackLink(session));
        
        model.addAttribute("dateTimeOriginal", metadata.getDateTimeOriginal());
        model.addAttribute("albums", metadata.getAlbums());
        model.addAttribute("description", metadata.getDescription());
        model.addAttribute("fileName", filename);
        model.addAttribute("editMode", true);
        
        return "file/index";
    }
    
    @PostMapping("/file/{file}/save")
    public String saveFile(HttpSession session, Model model, @PathVariable("file")String filename, @RequestParam("description") String newDescription) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.setDescription(archiveFile, newDescription);

        archiveIndexingService.reIndex(archiveFile);
        
        return "redirect:/file/" + filename;
    }
    
    @PostMapping("/file/{file}/album/add")
    public String addAlbum(HttpSession session, Model model, @PathVariable("file")String filename, @RequestParam("album") String album) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.addAlbum(archiveFile, new Album(album));

        archiveIndexingService.reIndex(archiveFile);
        
        return "redirect:/file/" + filename;
    }
    
    @PostMapping("/file/{file}/album/remove")
    public String removeAlbum(HttpSession session, Model model, @PathVariable("file")String filename, @RequestParam("album") String album) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.removeAlbum(archiveFile, new Album(album));

        archiveIndexingService.reIndex(archiveFile);
        
        return "redirect:/file/" + filename;
    }
    
	@ResponseBody
	@RequestMapping(value = "/file/{file}/raw", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] rawFile(@PathVariable("file")String filename) throws IOException {
		File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
		return FileUtils.readFileToByteArray(archiveFile);
	}
	
	private BackLink getBackLink(HttpSession session) {
		Object backLink = session.getAttribute(BackLink.class.getName());
		if(backLink instanceof BackLink) {
			return (BackLink) backLink;
		}
		return null;
	}
}
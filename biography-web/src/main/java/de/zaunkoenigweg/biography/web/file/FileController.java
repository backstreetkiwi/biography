package de.zaunkoenigweg.biography.web.file;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.index.IndexingService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;
import de.zaunkoenigweg.biography.web.BackLink;

@CrossOrigin
@Controller
public class FileController {

	private final static Log LOG = LogFactory.getLog(FileController.class);
	
    private final static byte[] PLACEHOLDER = {-1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 72, 0, 72, 0, 0, -1, -37, 0, 67, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -62, 0, 11, 8, 0, 1, 0, 1, 1, 1, 17, 0, -1, -60, 0, 20, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -1, -38, 0, 8, 1, 1, 0, 0, 0, 1, 95, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 1, 5, 2, 127, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 6, 63, 2, 127, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 1, 63, 33, 127, -1, -38, 0, 8, 1, 1, 0, 0, 0, 16, 127, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 1, 63, 16, 127, -1, -39 };
	private File archiveFolder;
	private File importFolder;
    private File thumbsFolder200;
    private File thumbsFolder300;

    private ArchiveMetadataService archiveMetadataService;
	private IndexingService indexingService;

	public FileController(File archiveFolder, File importFolder, ArchiveMetadataService archiveMetadataService, IndexingService indexingService) {
		this.archiveFolder = archiveFolder;
		this.importFolder = importFolder;
		this.archiveMetadataService = archiveMetadataService;
		this.indexingService = indexingService;
        this.thumbsFolder200 = new File(this.archiveFolder, "thumbnails/200");
        this.thumbsFolder300 = new File(this.archiveFolder, "thumbnails/300");
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

        indexingService.reIndex(archiveFile);
        
        return "redirect:/file/" + filename;
    }
    
    @PostMapping("/file/{file}/album/add")
    public String addAlbum(HttpSession session, Model model, @PathVariable("file")String filename, @RequestParam("album") String album) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.addAlbum(archiveFile, new Album(album));

        indexingService.reIndex(archiveFile);
        
        return "redirect:/file/" + filename;
    }
    
    @PostMapping("/file/{file}/album/remove")
    public String removeAlbum(HttpSession session, Model model, @PathVariable("file")String filename, @RequestParam("album") String album) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.removeAlbum(archiveFile, new Album(album));

        indexingService.reIndex(archiveFile);
        
        return "redirect:/file/" + filename;
    }
    
	@ResponseBody
	@RequestMapping(value = "/file/{file}/raw", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] rawFile(@PathVariable("file")String filename) throws IOException {
		File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
		return FileUtils.readFileToByteArray(archiveFile);
	}
	
	@ResponseBody
	@RequestMapping(value = "/file/{file}/200", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] thumbnail200(@PathVariable("file")String filename) throws IOException {
		File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(thumbsFolder200, filename);
        if(archiveFile.exists()) {
            return FileUtils.readFileToByteArray(archiveFile);
        }
        return PLACEHOLDER;
	}
	
    @ResponseBody
    @RequestMapping(value = "/file/{file}/300", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] thumbnail300(@PathVariable("file")String filename) throws IOException {
        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(thumbsFolder300, filename);
        if(archiveFile.exists()) {
            return FileUtils.readFileToByteArray(archiveFile);
        }
        return PLACEHOLDER;
    }
    
    @ResponseBody
    @RequestMapping(value = "/file/import/{file}/thumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> importThumbnail(@PathVariable("file")String filename) throws IOException {
        File thumbnailFile = new File(new File(this.importFolder, "thumbnails"), filename + ".jpg");
        if(thumbnailFile.exists()) {
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(thumbnailFile), HttpStatus.OK);
        }
        return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
    }
    
	private BackLink getBackLink(HttpSession session) {
		Object backLink = session.getAttribute(BackLink.class.getName());
		if(backLink instanceof BackLink) {
			return (BackLink) backLink;
		}
		return null;
	}
}
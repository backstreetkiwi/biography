package de.zaunkoenigweg.biography.web.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.zaunkoenigweg.biography.core.archive.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;

@Controller
public class FileController {

	private final static Log LOG = LogFactory.getLog(FileController.class);

	private File archiveFolder;
	
	private ArchiveMetadataService archiveMetadataService;

	public FileController(File archiveFolder, ArchiveMetadataService archiveMetadataService) {
		this.archiveFolder = archiveFolder;
		this.archiveMetadataService = archiveMetadataService;
		LOG.info("FileController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@RequestMapping("/file/{file}")
	public String fileDetails(Model model, @PathVariable("file")String filename) {

		File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
		
		BiographyMetadata metadata = archiveMetadataService.getMetadata(archiveFile);
		
		model.addAttribute("dateTimeOriginal", metadata.getDateTimeOriginal());
		model.addAttribute("albums", metadata.getAlbums());
		model.addAttribute("filename", filename);
		
		return "file/index";
	}
	
	@ResponseBody
	@RequestMapping(value = "/file/{file}/raw", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] rawFile(@PathVariable("file")String filename) throws IOException {
		File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
		return FileUtils.readFileToByteArray(archiveFile);
	}
}
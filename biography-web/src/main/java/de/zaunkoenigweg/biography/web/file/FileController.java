package de.zaunkoenigweg.biography.web.file;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.zaunkoenigweg.biography.core.MediaFileName;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.index.IndexingService;

@CrossOrigin
@Controller
public class FileController {

	private final static Log LOG = LogFactory.getLog(FileController.class);
	
	private File archiveFolder;
	private File importFolder;
    private File thumbsFolder200;
    private File thumbsFolder300;

	public FileController(File archiveFolder, File importFolder, ArchiveMetadataService archiveMetadataService, IndexingService indexingService) {
		this.archiveFolder = archiveFolder;
		this.importFolder = importFolder;
        this.thumbsFolder200 = new File(this.archiveFolder, "thumbnails/200");
        this.thumbsFolder300 = new File(this.archiveFolder, "thumbnails/300");
		LOG.info("FileController started.");
		LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
	}

	@ResponseBody
	@RequestMapping(value = "/file/{file}/raw", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> rawFile(@PathVariable("file")String filename) throws IOException {
		return file(filename, this.archiveFolder);
	}
	
	@ResponseBody
	@RequestMapping(value = "/file/{file}/200", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> thumbnail200(@PathVariable("file")String filename) throws IOException {
		return file(filename, this.thumbsFolder200);
	}
	
    @ResponseBody
    @RequestMapping(value = "/file/{file}/300", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> thumbnail300(@PathVariable("file")String filename) throws IOException {
		return file(filename, this.thumbsFolder300);
    }
    
	private ResponseEntity<byte[]> file(String filename, File baseFolder) throws IOException {
		if(!MediaFileName.isValid(filename)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		MediaFileName mediaFileName = MediaFileName.of(filename);
		File file = mediaFileName.archiveFile(baseFolder);
		if(!file.exists() || file.isDirectory()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(FileUtils.readFileToByteArray(file), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/file/import/{file}/thumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> importThumbnail(@PathVariable("file")UUID file) throws IOException {
		File thumbnailFile = new File(this.importFolder, "thumbnails/" + file + ".jpg");
		if(!thumbnailFile.exists() || thumbnailFile.isDirectory()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(FileUtils.readFileToByteArray(thumbnailFile), HttpStatus.OK);
	}
		
}
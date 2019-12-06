package de.zaunkoenigweg.biography.web.rest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.index.MediaFile;
import de.zaunkoenigweg.biography.core.index.SearchService;
import de.zaunkoenigweg.biography.core.index.SearchService.QueryMode;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;

@RestController
public class SearchRestController {

	private SearchService searchService;
	private File archiveFolder;
	
	public SearchRestController(SearchService searchService, File archiveFolder) {
		this.searchService = searchService;
		this.archiveFolder = archiveFolder;
	}

	@CrossOrigin
	@RequestMapping("/rest/search/")
	public Map<String,Object> search(@RequestParam(name="q", required=true) String query, 
			@RequestParam(name="mode", required=false, defaultValue="ALL") QueryMode queryMode) {

		Map<String, Object> restObject = new HashMap<>();
		List<Map<String, Object>> mediaFiles = searchService.findByDescription(query, queryMode).map(this::mediaFileToRestObject).collect(Collectors.toList());
		restObject.put("mediaFiles", mediaFiles);
		restObject.put("count", mediaFiles.size());
		return restObject;
	}

	// TODO duplicate from MediaFileRestController...
	private Map<String, Object> mediaFileToRestObject(MediaFile mediaFile) {
		MediaFileType mediaFileType = MediaFileType.of(BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder,mediaFile.getFileName())).get();
		String thumbnailFileName = StringUtils.replace(mediaFile.getFileName(), mediaFileType.getFileExtension(), "jpg");
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("fileName", mediaFile.getFileName());
		restObject.put("thumbnailFileName", thumbnailFileName);
		restObject.put("kind", mediaFileType.getKind());
		restObject.put("description", mediaFile.getDescription());
		restObject.put("albums", mediaFile.getAlbums());
		return restObject;
	}
}

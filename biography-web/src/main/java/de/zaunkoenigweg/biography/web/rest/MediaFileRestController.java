package de.zaunkoenigweg.biography.web.rest;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.zaunkoenigweg.biography.core.MediaFileType;
import de.zaunkoenigweg.biography.core.archivemetadata.ArchiveMetadataService;
import de.zaunkoenigweg.biography.core.index.IndexingService;
import de.zaunkoenigweg.biography.core.index.MediaFile;
import de.zaunkoenigweg.biography.core.index.SearchService;
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.Album;

@RestController
public class MediaFileRestController {

	private SearchService searchService;
	private ArchiveMetadataService archiveMetadataService;
	private IndexingService indexingService;
	private File archiveFolder;

	public MediaFileRestController(SearchService searchService, ArchiveMetadataService archiveMetadataService, IndexingService indexingService, File archiveFolder) {
		this.searchService = searchService;
		this.archiveMetadataService = archiveMetadataService;
		this.indexingService = indexingService;
		this.archiveFolder = archiveFolder;
	}

	@CrossOrigin
	@RequestMapping("/rest/mediafiles/")
	public List<Map<String, Object>> get() {
        return searchService.getYearCounts().map(this::yearCountToRestObject).collect(Collectors.toList());
	}
	
	@CrossOrigin
	@RequestMapping("/rest/mediafiles/{year}/")
	public List<Map<String, Object>> get(@PathVariable("year") Year year) {
        return searchService.getMonthCounts(year).map(this::monthCountToRestObject).collect(Collectors.toList());
	}
	
	@CrossOrigin
	@GetMapping("/rest/mediafiles/stats/")
	public Map<String, Object> getStats() {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("mostRecentYearMonth", searchService.getMostRecentYearMonth().map(YearMonth::toString).orElse(""));
		return restObject;
	}
	
	@CrossOrigin
	@RequestMapping("/rest/mediafiles/{year}/{month}")
	public List<Map<String, Object>> get(@PathVariable("year") Year year, @PathVariable("month") int month) {
        YearMonth yearMonth = YearMonth.of(year.getValue(), Month.of(month));
        return searchService.getDayCounts(yearMonth).map(this::dayCountToRestObject).collect(Collectors.toList());
	}
	
	@CrossOrigin
	@RequestMapping("/rest/mediafiles/{year}/{month}/{day}/")
	public Map<String,Object> get(@PathVariable("year") Year year, @PathVariable("month") int month, @PathVariable("day") int day) {

		LocalDate localDate = LocalDate.of(year.getValue(), month, day);
		
		Map<String, Object> restObject = new HashMap<>();
		List<Map<String, Object>> mediaFiles = searchService.findByDate(localDate).map(this::mediaFileToRestObject).collect(Collectors.toList());
		restObject.put("mediaFiles", mediaFiles);
		restObject.put("count", mediaFiles.size());
		return restObject;
	}
	
	@CrossOrigin
    @PutMapping("/rest/file/{file}")
    public String putFileAttributes(HttpSession session, Model model, @PathVariable("file")String filename, @RequestParam("description") String newDescription) {

        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.setDescription(archiveFile, newDescription);

        indexingService.reIndex(archiveFile);
        
        return "";
    }
    
	@CrossOrigin
    @PostMapping("/rest/file/{file}/albums/{album}/")
    public String postAlbum(HttpSession session, Model model, @PathVariable("file")String filename, @PathVariable("album")String album) {

		if(StringUtils.isBlank(album)) {
			return "";
		}
		
        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.addAlbum(archiveFile, new Album(album.trim()));

        indexingService.reIndex(archiveFile);
        
        return "";
    }
    
	@CrossOrigin
    @DeleteMapping("/rest/file/{file}/albums/{album}/")
    public String deleteAlbum(HttpSession session, Model model, @PathVariable("file")String filename, @PathVariable("album")String album) {

		if(StringUtils.isBlank(album)) {
			return "";
		}
		
        File archiveFile = BiographyFileUtils.getArchiveFileFromShortFilename(archiveFolder, filename);
        
        archiveMetadataService.removeAlbum(archiveFile, new Album(album.trim()));

        indexingService.reIndex(archiveFile);
        
        return "";
    }
    
    private Map<String, Object> yearCountToRestObject(Pair<Year, Long> yearCount) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("year", yearCount.getLeft());
		restObject.put("count", yearCount.getRight());
		return restObject;
	}
	
	private Map<String, Object> monthCountToRestObject(Pair<YearMonth, Long> monthCount) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("yearMonth", monthCount.getLeft());
		restObject.put("count", monthCount.getRight());
		return restObject;
	}
	
	private Map<String, Object> dayCountToRestObject(Pair<LocalDate, Long> dayCount) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("date", dayCount.getLeft());
		restObject.put("count", dayCount.getRight());
		return restObject;
	}
	
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

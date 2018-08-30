package de.zaunkoenigweg.biography.web.rest;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.zaunkoenigweg.biography.core.index.ArchiveSearchService;
import de.zaunkoenigweg.biography.core.index.MediaFile;

@RestController
public class MediaFileRestController {

	private ArchiveSearchService archiveSearchService;

	public MediaFileRestController(ArchiveSearchService archiveSearchService) {
		this.archiveSearchService = archiveSearchService;
	}

	@CrossOrigin
	@RequestMapping("/rest/mediafiles/")
	public List<Map<String, Object>> get() {
        return archiveSearchService.getYearCounts().map(this::yearCountToRestObject).collect(Collectors.toList());
	}
	
	@CrossOrigin
	@RequestMapping("/rest/mediafiles/{year}/")
	public List<Map<String, Object>> get(@PathVariable("year") Year year) {
        return archiveSearchService.getMonthCounts(year).map(this::monthCountToRestObject).collect(Collectors.toList());
	}
	
	@CrossOrigin
	@RequestMapping("/rest/mediafiles/{year}/{month}")
	public List<Map<String, Object>> get(@PathVariable("year") Year year, @PathVariable("month") int month) {
        YearMonth yearMonth = YearMonth.of(year.getValue(), Month.of(month));
        return archiveSearchService.getDayCounts(yearMonth).map(this::dayCountToRestObject).collect(Collectors.toList());
	}
	
	@CrossOrigin
	@RequestMapping("/rest/mediafiles/{year}/{month}/{day}/")
	public Map<String,Object> get(@PathVariable("year") Year year, @PathVariable("month") int month, @PathVariable("day") int day) {

		LocalDate localDate = LocalDate.of(year.getValue(), month, day);
		
		Map<String, Object> restObject = new HashMap<>();
		List<Map<String, Object>> mediaFiles = archiveSearchService.findByDate(localDate).map(this::mediaFileToRestObject).collect(Collectors.toList());
		restObject.put("mediaFiles", mediaFiles);
		restObject.put("count", mediaFiles.size());
		return restObject;
	}
	
	Map<String, Object> yearCountToRestObject(Pair<Year, Long> yearCount) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("year", yearCount.getLeft());
		restObject.put("count", yearCount.getRight());
		return restObject;
	}
	
	Map<String, Object> monthCountToRestObject(Pair<YearMonth, Long> monthCount) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("yearMonth", monthCount.getLeft());
		restObject.put("count", monthCount.getRight());
		return restObject;
	}
	
	Map<String, Object> dayCountToRestObject(Pair<LocalDate, Long> dayCount) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("date", dayCount.getLeft());
		restObject.put("count", dayCount.getRight());
		return restObject;
	}
	
	Map<String, Object> mediaFileToRestObject(MediaFile mediaFile) {
		Map<String, Object> restObject = new HashMap<>();
		restObject.put("fileName", mediaFile.getFileName());
		restObject.put("description", mediaFile.getDescription());
		return restObject;
	}
	
}
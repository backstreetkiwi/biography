package de.zaunkoenigweg.biography.web.timeline;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.index.ArchiveSearchService;
import de.zaunkoenigweg.biography.core.index.MediaFile;

@Controller
public class TimelineController {

    private final static Log LOG = LogFactory.getLog(TimelineController.class);

    private File archiveFolder;

    private ArchiveSearchService archiveSearchService;

    public TimelineController(File archiveFolder, ArchiveSearchService archiveSearchService) {
        this.archiveFolder = archiveFolder;
        this.archiveSearchService = archiveSearchService;
        LOG.info("TimelineController started.");
        LOG.info(String.format("archiveFolder=%s", this.archiveFolder));
    }

    @RequestMapping("/timeline")
    public String timeline(Model model) {

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());

        model.addAttribute("years", years);

        return "timeline/index";
    }

    @RequestMapping("/timeline/{year}")
    public String timeline(Model model, @PathVariable("year") Year year) {

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());
        
        List<Pair<YearMonth, Long>> months = archiveSearchService.getMonthCounts(year).collect(Collectors.toList());

        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("selectedYear", year);

        return "timeline/index";
    }

    @RequestMapping("/timeline/{year}/{month}")
    public String timeline(Model model, @PathVariable("year") Year year, @PathVariable("month") int month) {

        YearMonth yearMonth = YearMonth.of(year.getValue(), Month.of(month));

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());
        List<Pair<YearMonth, Long>> months = archiveSearchService.getMonthCounts(year).collect(Collectors.toList());
        List<Pair<LocalDate, Long>> days = archiveSearchService.getDayCounts(yearMonth).collect(Collectors.toList());

        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("days", days);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", yearMonth);

        return "timeline/index";
    }

    @RequestMapping("/timeline/{year}/{month}/{day}")
    public String timeline(Model model, @PathVariable("year") Year year, @PathVariable("month") int month, @PathVariable("day") int day) {

        LocalDate localDate = LocalDate.of(year.getValue(), month, day);

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());
        List<Pair<YearMonth, Long>> months = archiveSearchService.getMonthCounts(year).collect(Collectors.toList());
        List<Pair<LocalDate, Long>> days = archiveSearchService.getDayCounts(YearMonth.from(localDate)).collect(Collectors.toList());
        
        List<MediaFile> mediaFiles = archiveSearchService.findByDate(localDate).collect(Collectors.toList());

        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("days", days);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", YearMonth.from(localDate));
        model.addAttribute("selectedDay", localDate);
        model.addAttribute("mediaFiles", mediaFiles);

        return "timeline/index";
    }

}

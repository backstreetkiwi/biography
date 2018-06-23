package de.zaunkoenigweg.biography.web.timeline;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.core.index.ArchiveSearchService;
import de.zaunkoenigweg.biography.core.index.MediaFile;
import de.zaunkoenigweg.biography.web.BackLink;

@Controller
public class TimelineController {

    private final static Log LOG = LogFactory.getLog(TimelineController.class);

    private ArchiveSearchService archiveSearchService;
    
    public TimelineController(ArchiveSearchService archiveSearchService) {
        this.archiveSearchService = archiveSearchService;
        LOG.info("TimelineController started.");
    }

    @RequestMapping("/")
    public String home() {
        return "redirect:/timeline";
    }

    @RequestMapping("/timeline")
    public String timeline(Model model) {

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());

        model.addAttribute("years", years);
        model.addAttribute("selectedMenuItem", "TIMELINE");

        return "timeline/index";
    }

    @RequestMapping("/timeline/{year}")
    public String timeline(Model model, @PathVariable("year") Year year) {

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());

        List<Pair<YearMonth, Long>> months = archiveSearchService.getMonthCounts(year).collect(Collectors.toList());

        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMenuItem", "TIMELINE");

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
        model.addAttribute("selectedMenuItem", "TIMELINE");

        return "timeline/index";
    }

    @RequestMapping("/timeline/{year}/{month}/{day}")
    public String timeline(HttpSession session, HttpServletRequest request, Model model, @PathVariable("year") Year year, @PathVariable("month") int month,
            @PathVariable("day") int day) {

        LocalDate localDate = LocalDate.of(year.getValue(), month, day);

        List<Pair<Year, Long>> years = archiveSearchService.getYearCounts().collect(Collectors.toList());
        List<Pair<YearMonth, Long>> months = archiveSearchService.getMonthCounts(year).collect(Collectors.toList());
        List<Pair<LocalDate, Long>> days = archiveSearchService.getDayCounts(YearMonth.from(localDate))
                .collect(Collectors.toList());

        List<MediaFile> mediaFiles = archiveSearchService.findByDate(localDate).collect(Collectors.toList());

        BackLink backLink = new BackLink("BACK TO TIMELINE", request.getRequestURI());
		session.setAttribute(BackLink.class.getName(), backLink);
        
        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("days", days);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", YearMonth.from(localDate));
        model.addAttribute("selectedDay", localDate);
        model.addAttribute("mediaFiles", mediaFiles);
        model.addAttribute("selectedMenuItem", "TIMELINE");

        return "timeline/index";
    }

}

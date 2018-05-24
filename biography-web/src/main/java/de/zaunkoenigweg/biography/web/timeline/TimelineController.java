package de.zaunkoenigweg.biography.web.timeline;

import java.io.File;
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
import de.zaunkoenigweg.biography.core.util.BiographyFileUtils;
import de.zaunkoenigweg.biography.metadata.BiographyMetadata;

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

        List<Pair<Year, Long>> years = archiveSearchService.getYearCount().collect(Collectors.toList());

//		List<File> mediaFiles = BiographyFileUtils.getMediaFiles(archiveFolder);
//		
//		List<String> list = mediaFiles.stream().map(File::getName).collect(Collectors.toList());
//		
//		BiographyFileUtils.getDatetimeOriginalFromArchiveFilename(mediaFiles.stream().findFirst().get());

        model.addAttribute("years", years);

        return "timeline/index";
    }

    @RequestMapping("/timeline/{year}")
    public String fileDetails(Model model, @PathVariable("year") Year year) {

        List<Pair<Year, Long>> years = archiveSearchService.getYearCount().collect(Collectors.toList());

        List<Pair<YearMonth, Long>> months = archiveSearchService.getMonthCount(year).collect(Collectors.toList());

        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("selectedYear", year);

        return "timeline/index";
    }


}

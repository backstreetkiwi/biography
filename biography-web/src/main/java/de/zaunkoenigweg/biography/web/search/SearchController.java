package de.zaunkoenigweg.biography.web.search;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.zaunkoenigweg.biography.core.index.ArchiveSearchService;
import de.zaunkoenigweg.biography.core.index.MediaFile;

@Controller
public class SearchController {

    private final static Log LOG = LogFactory.getLog(SearchController.class);

    private ArchiveSearchService archiveSearchService;

    public SearchController(ArchiveSearchService archiveSearchService) {
        this.archiveSearchService = archiveSearchService;
        LOG.info("SearchController started.");
    }

    @RequestMapping("/search")
    public String timeline(Model model) {

        model.addAttribute("selectedMenuItem", "SEARCH");
        model.addAttribute("q", "");

        return "search/index";
    }

    @RequestMapping("/search/query/")
    public String timeline(Model model, @RequestParam("q") String queryString) {

        List<MediaFile> mediaFiles = archiveSearchService.findByDescription(queryString).collect(Collectors.toList());;
        
        model.addAttribute("selectedMenuItem", "SEARCH");
        model.addAttribute("q", queryString);
        model.addAttribute("mediaFiles", mediaFiles);

        return "search/index";
    }

}

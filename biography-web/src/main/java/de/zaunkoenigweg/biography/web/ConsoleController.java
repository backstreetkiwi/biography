package de.zaunkoenigweg.biography.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.zaunkoenigweg.biography.web.console.Console;
import de.zaunkoenigweg.biography.web.console.Consoles;

@Controller
public class ConsoleController {

	private Consoles consoles;
	
    public ConsoleController(Consoles consoles) {
		this.consoles = consoles;
	}

	@RequestMapping("/console/{consoleid}")
    public String console(Model model, @PathVariable("consoleid")Integer consoleId) {
		Console console = consoles.get(consoleId);
		model.addAttribute("title", console.getTitle());
		model.addAttribute("closed", console.isClosed());
		model.addAttribute("startTime", console.getStartTime());
		model.addAttribute("content", console.getContent());
        return "console/console";
    }

	@RequestMapping("/console")
    public String consoles(Model model) {
		model.addAttribute("consoles", consoles.getAll());
        return "console/index";
    }

}
package de.zaunkoenigweg.biography.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ConsoleController {

	private Console console;
	
    public ConsoleController(Console console) {
		this.console = console;
	}

	@RequestMapping("/console")
    public String console(Model model) {
		model.addAttribute("consoleContent", console.getContent());
        return "console";
    }

}
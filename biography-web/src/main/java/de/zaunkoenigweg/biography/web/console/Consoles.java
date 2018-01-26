package de.zaunkoenigweg.biography.web.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class Consoles {

	private final static Log LOG = LogFactory.getLog(Consoles.class);

	private List<Console> consoles;
	
	public Consoles() {
		this.consoles = new ArrayList<>();
		LOG.info("Consoles (Bean) started.");
	}
	

	public Console create(String title) {
		Console console = new Console(title);
		consoles.add(console);
		return console;
	}
	
	public Console get(int index) {
		return consoles.get(index);
	}
	
	public List<Console> getAll() {
		// TODO can streams be passed to thymeleaf views?
		return Collections.unmodifiableList(consoles);
	}
}

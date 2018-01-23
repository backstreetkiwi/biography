package de.zaunkoenigweg.biography.web;

import java.io.StringWriter;
import java.io.Writer;

import org.springframework.stereotype.Component;

@Component
public class Console {

	private StringWriter writer;

	public Console() {
		this.writer = new StringWriter();
	}
	
//	public void clear() {
//		this.writer = new StringWriter();
//	}
	
	public Writer writer() {
		return writer;
	}

	public String getContent() {
		return writer.toString();
	}
}

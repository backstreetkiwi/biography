package de.zaunkoenigweg.biography.web.console;

import java.time.LocalDateTime;

public class Console {

	private String title;
	
	private boolean closed;
	
	private LocalDateTime startTime;
	
	private StringBuffer buffer;
	
	public Console(String title) {
		this.buffer = new StringBuffer();
		this.title = title;
		this.startTime = LocalDateTime.now();
	}

	public void println(String line) {
		if(closed) {
			throw new IllegalStateException(String.format("Console %s is already closed!", title));
		}
		buffer.append(line);
		buffer.append("\n");
	}
	
	public boolean isClosed() {
		return closed;
	}

	public void close() {
		this.closed = true;
	}

	public String getTitle() {
		return title;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public String getContent() {
		return buffer.toString();
	}
}

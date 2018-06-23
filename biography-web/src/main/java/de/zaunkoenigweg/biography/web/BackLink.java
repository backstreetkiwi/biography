package de.zaunkoenigweg.biography.web;

public class BackLink {

	private String text;
	private String href;
	
	public BackLink(String text, String href) {
		this.text = text;
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public String getText() {
		return text;
	}
	
}

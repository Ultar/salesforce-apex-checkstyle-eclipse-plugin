package com.salesforce.apex.checkstyle.plugin.config;

import java.util.regex.Pattern;

public class Marker {
	private Pattern regex;
	private String message;
	private String replacement;

	public Marker(Pattern regex, String message, String replacement) {
		this.regex = regex;
		this.message = message;
		this.replacement = replacement;
	}
	
	public Pattern getRegex() {
		return regex;
	}
	public String getMessage() {
		return message;
	}
	public String getReplacement() {
		return replacement;
	}
}

package com.salesforce.apex.checkstyle.plugin.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ApexCheckstyleConfiguration {

	private final static String XML_TAG_FILE_EXTENSIONS = "fileExtensions";
	private final static String XML_TAG_MARKER = "marker";
	private final static String XML_TAG_REGEX = "regex";
	private final static String XML_TAG_MESSAGE = "message";
	private final static String XML_TAG_REPLACEMENT = "replacement";
	
	private String[] fileExtensions;
	
	private Set<Marker> markers;
	
	
	public ApexCheckstyleConfiguration(File configFile) {
		markers = new HashSet<Marker>();
		parseConfig(configFile);
	}
	
	
	private void parseConfig(File configFile) {
		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.
					newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(configFile);
	 
			String extensions = getTagValue(XML_TAG_FILE_EXTENSIONS, doc.getDocumentElement());
			if (extensions != null && !extensions.isEmpty()) {
				fileExtensions = extensions.split("\\s*,\\s*");
			}
			
			NodeList markersList = doc.getElementsByTagName(XML_TAG_MARKER);
			
			for (int marker = 0; marker < markersList.getLength(); marker++) {
				Node markerNode = markersList.item(marker);
				if (markerNode.getNodeType() == Node.ELEMENT_NODE) {
					String regex = getTagValue(XML_TAG_REGEX, (Element)markerNode);
					String message = getTagValue(XML_TAG_MESSAGE, (Element)markerNode);
					String replacement = getTagValue(XML_TAG_REPLACEMENT, (Element)markerNode);

					if (regex != null && message != null && !regex.isEmpty() && !message.isEmpty()) {
						try {
							Pattern pattern = Pattern.compile(regex);
							Marker markObj = new Marker(pattern, message, replacement);
							markers.add(markObj);
						} catch (PatternSyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			}
		  } catch (Exception e) {
			e.printStackTrace();
		  }
	}
	
	private String getTagValue(String tag, Element element) {
		if (element.getElementsByTagName(tag) == null || 
				element.getElementsByTagName(tag).item(0) == null)
	    	return null;		
	  
		NodeList list = element.getElementsByTagName(tag).item(0).getChildNodes();
	    Node value = (Node) list.item(0);
	 
		if (value == null)
	    	return null;
	    
		return value.getNodeValue();
	}


	public String[] getFileExtensions() {
		return fileExtensions;
	}


	public Set<Marker> getMarkers() {
		return markers;
	}

}

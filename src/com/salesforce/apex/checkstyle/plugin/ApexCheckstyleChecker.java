package com.salesforce.apex.checkstyle.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.salesforce.apex.checkstyle.plugin.config.ApexCheckstyleConfiguration;
import com.salesforce.apex.checkstyle.plugin.config.Marker;


public class ApexCheckstyleChecker {

	private static final String MARKER_ID = ApexCheckStylePlugin.PLUGIN_ID + ".apexCheckstyleMarker";
	
	public static void markFile(ApexCheckstyleConfiguration config, IFile sourceFile, boolean replace) {
		
    	boolean rightFileExtension = false; 
    	for (String extension : config.getFileExtensions()) {
    		if (extension.equalsIgnoreCase(sourceFile.getFileExtension()))
    			rightFileExtension = true;
    	}
    	if (!rightFileExtension)
    		return;
    	
    	// replacing markers first
    	Set<Integer> lineMarkers = new HashSet<Integer>();
    	if (replace) {
    		IMarker[] markers = null;
			try {
				markers = sourceFile.findMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
	    		if (markers != null) {
	    			for (IMarker marker : markers) {
	    				lineMarkers.add(marker.getAttribute(IMarker.LINE_NUMBER, -1));
	    			}
	    		}
	    		sourceFile.deleteMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
    		
        	StringBuilder newContent = null;
        	try {
    			BufferedReader br = 
    					new BufferedReader(new InputStreamReader(
    							sourceFile.getContents()));

    			String line;
    			int lineNumber = 1;
    			boolean commentedLine = false, fileChanged = false;
    			while( (line = br.readLine()) != null) {
    				String sourceLine = line;
    				
    				int slashComment = line.indexOf("//");
    				if (slashComment > -1) {
    					line = line.substring(0, slashComment);
    				}
    				
    				line = line.replaceAll("/\\*.*\\*/", "");
    				
    				int startComment = line.indexOf("/*");
    				if (startComment > -1) {
    					line = line.substring(0, startComment);
    				}

    				int endComment = line.indexOf("*/");
    				if (endComment > -1) {
    					line = line.substring(endComment + "*/".length());
    				}
    				
    				if ((startComment > endComment) ||
    						(startComment == -1 && endComment > -1)) {
    					commentedLine = false;
    				}
    				
    				if (!commentedLine) {
    					for (Marker checkMarker : config.getMarkers()) {
    						Matcher matcher = checkMarker.getRegex().matcher(line);
    						if (matcher.matches()) {
    							String replacement = checkMarker.getReplacement();
    							if (replacement != null && matcher.groupCount() == 1 &&
    								lineMarkers.size() > 0 && lineMarkers.contains(lineNumber)) {
    								fileChanged = true;
    								sourceLine = sourceLine.replace(matcher.group(1), replacement);
    							}
    						}
    					}
    				}
    				
    				if (startComment > endComment) {
    					commentedLine = true;
    				}
    				
    				lineNumber++;
    				
   					if (newContent == null)
   						newContent = new StringBuilder();
   					newContent.append(sourceLine + "\n");
    			}
    			
    			if (fileChanged) {
	   	    		InputStream is = new ByteArrayInputStream(newContent.toString().getBytes());
	   				sourceFile.setContents(is, true, true, null);
	   				// blocking double check
	   				ApexCheckStylePlugin.lockListener();
    			}
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    		} 
    	}
    	
    	// mark file
    	try {
    		sourceFile.deleteMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
    	
    	try {
			BufferedReader br = 
					new BufferedReader(new InputStreamReader(
							sourceFile.getContents()));

			String line;
			int lineNumber = 1;
			boolean commentedLine = false;
			while( (line = br.readLine()) != null) {
				
				int slashComment = line.indexOf("//");
				if (slashComment > -1) {
					line = line.substring(0, slashComment);
				}
				
				line = line.replaceAll("/\\*.*\\*/", "");
				
				int startComment = line.indexOf("/*");
				if (startComment > -1) {
					line = line.substring(0, startComment);
				}

				int endComment = line.indexOf("*/");
				if (endComment > -1) {
					line = line.substring(endComment + "*/".length());
				}
				
				if ((startComment > endComment) ||
						(startComment == -1 && endComment > -1)) {
					commentedLine = false;
				}

				if (!commentedLine) {
					for (Marker checkMarker : config.getMarkers()) {
						Matcher matcher = checkMarker.getRegex().matcher(line);
						if (matcher.matches()) {
							// if line wasn't marked before replace, then it can't be marked
							if (replace) {
								if (lineMarkers.size() > 0 && lineMarkers.contains(lineNumber)) {
									IMarker marker = sourceFile.createMarker(MARKER_ID);
									marker.setAttribute(IMarker.MESSAGE, checkMarker.getMessage());
									marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
									marker.setAttribute(IMarker.LINE_NUMBER, Integer.valueOf(lineNumber));
								}
							} else {
								IMarker marker = sourceFile.createMarker(MARKER_ID);
								marker.setAttribute(IMarker.MESSAGE, checkMarker.getMessage());
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
								marker.setAttribute(IMarker.LINE_NUMBER, Integer.valueOf(lineNumber));
							}
							
							
						}
					}
				}

				if (startComment > endComment) {
					commentedLine = true;
				}
				
				lineNumber++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}

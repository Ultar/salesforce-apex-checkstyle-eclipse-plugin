package com.salesforce.apex.checkstyle.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.salesforce.apex.checkstyle.plugin.config.ApexCheckstyleConfiguration;

public class ApexCheckstyleOnFilesJob extends WorkspaceJob implements ISchedulingRule {

	private static final String APEX_CHECKSTYLE_CONFIG_FILE = "apexcheckstyle-config.xml";
	
	private List<IFile> filesToCheck;
	private boolean replace;
	
	public ApexCheckstyleOnFilesJob(List<IFile> files, boolean replace) {
		super("Apex Checkstyle Workspace Job");
		filesToCheck = new ArrayList<IFile>(files);
		this.replace = replace;
		
        setRule(this);
	}
	
	public ApexCheckstyleOnFilesJob(IFile file, boolean replace) {
		super("Apex Checkstyle Workspace Job");
		filesToCheck = new ArrayList<IFile>();
		filesToCheck.add(file);
		this.replace = replace;

        setRule(this);
	}

	@Override
	public boolean contains(ISchedulingRule arg0) {
		return arg0 instanceof ApexCheckstyleOnFilesJob;
	}

	@Override
	public boolean isConflicting(ISchedulingRule arg0) {
		return arg0 instanceof ApexCheckstyleOnFilesJob;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
		
		ApexCheckstyleConfiguration config = getPluginConfiguration();
		
		for (IFile resource : filesToCheck)
			ApexCheckstyleChecker.markFile(config, resource, replace);
    	
		return Status.OK_STATUS;		
	}

	private ApexCheckstyleConfiguration getPluginConfiguration() {
        IPath configPath = ApexCheckStylePlugin.getDefault().getStateLocation();
        configPath = configPath.append(APEX_CHECKSTYLE_CONFIG_FILE);
        File configFile = configPath.toFile();
    	if (!configFile.exists()) {
    		try {
				configFile.createNewFile();
				FileWriter fw = new FileWriter(configFile);
				fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
						 "\n<apexcheckstyle-configuration>" +
						 "\n\t<fileExtensions>java, cls</fileExtensions>" +
						 "\n\t<apexcheckstyle-markers>" +
						 "\n\t\t<marker>" +
						 "\n\t\t\t<regex>.*if\\(.*</regex>" +
						 "\n\t\t\t<message>Whitespace near 'if'</message>" +
						 "\n\t\t</marker>" +
						 "\n\t\t<marker>" +
						 "\n\t\t\t<regex>.*(for\\().*</regex>" +
						 "\n\t\t\t<message>Whitespace near 'for'</message>" + 
						 "\n\t\t\t<replacement>for (</replacement>" +						 
						 "\n\t\t</marker>" + 
						 "\n\t</apexcheckstyle-markers>" +
						 "\n</apexcheckstyle-configuration>");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	return new ApexCheckstyleConfiguration(configFile);
	}
}

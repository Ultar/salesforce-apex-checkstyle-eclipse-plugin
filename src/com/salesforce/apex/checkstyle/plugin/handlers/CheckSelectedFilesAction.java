package com.salesforce.apex.checkstyle.plugin.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.salesforce.apex.checkstyle.plugin.ApexCheckstyleOnFilesJob;


public class CheckSelectedFilesAction implements IObjectActionDelegate {

	private static final String REPLACE_ACTION_ID = 
			"com.salesforce.apex.checkstyle.plugin.handlers.ReplaceSelectedFilesAction";
	
	@SuppressWarnings("unused")
	private IWorkbenchPart part;
	
	private IStructuredSelection selection;
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(IAction action) {
		
	    boolean replace = false;
	    if (action.getId().equals(REPLACE_ACTION_ID)) {
	    	replace = true;
	    }
	    
	    List<IFile> filesToCheck = new ArrayList<IFile>();
	    
	    try {
			addFileResources(selection.toList(), filesToCheck);
			
        	ApexCheckstyleOnFilesJob job = 
        			new ApexCheckstyleOnFilesJob(filesToCheck, replace);
        	job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        	job.schedule();
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
        	this.selection = (IStructuredSelection) selection;
        }
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        part = targetPart;
	}
	
    /**
     * Recursivly add all files contained in the given resource collection to
     * the second list.
     * 
     * @param resources list of resource
     * @param files the list of files
     * @throws CoreException en unexpected exception
     */
    private void addFileResources(List<IResource> resources, List<IFile> files)
        throws CoreException {

        for (IResource resource : resources) {
        	
            if (!resource.isAccessible()) {
                continue;
            }

            if (resource instanceof IFile) {
                files.add((IFile) resource);
            }
            else if (resource instanceof IContainer) {
                addFileResources(Arrays.asList(((IContainer) resource).members()), files);
            }
        }
    }
}

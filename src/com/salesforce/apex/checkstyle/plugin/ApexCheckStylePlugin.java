package com.salesforce.apex.checkstyle.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class ApexCheckStylePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.salesforce.apex.checkstyle.plugin"; //$NON-NLS-1$

	// The shared instance
	private static ApexCheckStylePlugin plugin;

	private static boolean listenerLocked;
	
	/**
	 * The constructor
	 */
	public ApexCheckStylePlugin() {
		super();
		plugin = this;
		listenerLocked = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
	   IWorkspace workspace = ResourcesPlugin.getWorkspace();
	   
	   IResourceChangeListener listener = new IResourceChangeListener() {
		  @Override
	      public void resourceChanged(IResourceChangeEvent event) {
			  // blocking double check after replacing
			  if (!listenerLocked) {
		         IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
		             public boolean visit(IResourceDelta delta) {
		                //only interested in changed resources (not added or removed)
		                if (delta.getKind() != IResourceDelta.CHANGED)
		                   return true;
		                //only interested in content changes
		                if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
		                   return true;
		                IResource resource = delta.getResource();
		                //only interested in files
		                if (resource.getType() == IResource.FILE) {
		                	ApexCheckstyleOnFilesJob job = 
		                			new ApexCheckstyleOnFilesJob((IFile)resource, false);
		                	job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		                	job.schedule();
		                }
		                return true;
		             }
		          };
				try {
					event.getDelta().accept(visitor);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
			listenerLocked = false;
	      }
		
	   };
	   
	   workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ApexCheckStylePlugin getDefault() {
		return plugin;
	}
	
	public static void lockListener() {
		listenerLocked = true;
	}
}

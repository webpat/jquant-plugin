package org.jquant.plugin;

import java.net.URL;
import java.rmi.activation.Activator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class JQuantPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID= "org.jquant.plugin";

	private static final IPath ICONS_PATH = new Path("$nl$/icons");;
	
	private static JQuantPlugin fPlugin = null;
	
	//Log4J logger
	private static final Logger logger = Logger.getLogger(Activator.class);

	private static JQuantLaunchListener fLaunchListener;
	
	public JQuantPlugin(){
		fPlugin = this;
		fLaunchListener = new JQuantLaunchListener();
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(fLaunchListener);
		logger.debug("JQuant Plugin started");
	}

	
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		logger.debug("JQuant Plugin stoped");
	}
	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow workBenchWindow= getActiveWorkbenchWindow();
		if (workBenchWindow == null)
			return null;
		return workBenchWindow.getShell();
	}
	
	/**
	 * Returns the active workbench window
	 *
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (fPlugin == null)
			return null;
		IWorkbench workBench= fPlugin.getWorkbench();
		if (workBench == null)
			return null;
		return workBench.getActiveWorkbenchWindow();
	}
	
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow= getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return null;
		return activeWorkbenchWindow.getActivePage();
	}
	
	public static String getPluginId() {
		return PLUGIN_ID;
	}
	
	
	/**
	 * Get a view from the registry
	 * @param viewID
	 * @return a {@link IViewPart} implementing View with the viewID ID 
	 */
	public static IViewPart getView(String viewID){
		IViewReference[] refs = getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference viewReference: refs){
			if (viewReference.getId().equals(viewID)){
				return viewReference.getView(true);
			}
		}
		return null;
	}


	/**
	 * Creates an image descriptor for the given path in a bundle. The path can
	 * contain variables like $NL$. If no image could be found,
	 * <code>useMissingImageDescriptor</code> decides if either the 'missing
	 * image descriptor' is returned or <code>null</code>.
	 *
	 * @param bundle a bundle
	 * @param path path in the bundle
	 * @param useMissingImageDescriptor if <code>true</code>, returns the shared image descriptor
	 *            for a missing image. Otherwise, returns <code>null</code> if the image could not
	 *            be found
	 * @return an {@link ImageDescriptor}, or <code>null</code> iff there's
	 *         no image at the given location and
	 *         <code>useMissingImageDescriptor</code> is <code>true</code>
	 */
	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url= FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}
	
	public static ImageDescriptor getImageDescriptor(String relativePath) {
		IPath path= ICONS_PATH.append(relativePath);
		return createImageDescriptor(fPlugin.getBundle(), path, true);
	}
	
	
	
	public static void displayStrategyResultView(){
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				
				
				IWorkbenchPage page = getActivePage();
				if (page != null){

					StrategyResultView existingView= (StrategyResultView) page.findView(StrategyResultView.ID);
					if (existingView == null) {
					
						try {
							existingView = (StrategyResultView) page.showView(StrategyResultView.ID, null, IWorkbenchPage.VIEW_CREATE);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}else{
						existingView.refresh(); 
					}
					
					page.activate(existingView);
				}
			}
		});
	}
	
	
	private static Display getDisplay() {
	
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;
	}
}

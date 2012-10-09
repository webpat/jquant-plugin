package org.jquant.plugin.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.jquant.plugin.JQuantPlugin;

public class BacktestLaunchShortcut implements ILaunchShortcut2 {

	private static final String EMPTY_STRING= "";
	
	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			launch(((IStructuredSelection) selection).toArray(), mode);
		} else {
			showNoStrategiesFoundDialog();
		}
	}
	
	
	@Override
	public void launch(IEditorPart editor, String mode) {
		ITypeRoot element= JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		
		if (element != null){
			launch(new Object[]{element},mode);
		}else{
			showNoStrategiesFoundDialog();
		}
	}
	
	

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		// TODO Search Launch Configuration
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		// TODO Search Launch Configuration
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		// TODO Search Launchable Type
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		// TODO Search Launchable Type
		return null;
	}
	
	private void launch(Object[] elements, String mode){
		try {
			IJavaElement elementToLaunch= null;

			if (elements.length == 1) {
				Object selected= elements[0];
				if (!(selected instanceof IJavaElement) && selected instanceof IAdaptable) {
					selected= ((IAdaptable) selected).getAdapter(IJavaElement.class);
				}
				if (selected instanceof IJavaElement) {
					IJavaElement element= (IJavaElement) selected;
					switch (element.getElementType()) {
						case IJavaElement.TYPE:
							elementToLaunch= element;
							break;
						case IJavaElement.CLASS_FILE:
							elementToLaunch= ((IClassFile) element).getType();
							break;
						case IJavaElement.COMPILATION_UNIT:
							elementToLaunch= ((ICompilationUnit)element).getTypes()[0];
							break;
					}
				}
			}
			if (elementToLaunch == null) {
				showNoStrategiesFoundDialog();
				return;
			}
			performLaunch(elementToLaunch, mode);
		} catch (InterruptedException e) {
			// OK, silently move on
		} catch (CoreException e) {
			// TODO: LAUNCH FAILED Msg 
		}
	}
	
	
	

	

	private void performLaunch(IJavaElement element, String mode) throws InterruptedException, CoreException {
		ILaunchConfigurationWorkingCopy temp= createLaunchConfiguration(element);
		ILaunchConfiguration config= findExistingLaunchConfiguration(temp, mode);
		if (config == null) {
			// no existing found: create a new one
			config= temp.doSave();
		}
		DebugUITools.launch(config, mode);
	}
	
	/**
	 * Creates a launch configuration working copy for the given element. The launch configuration type created will be of the type returned by {@link #getLaunchConfigurationTypeId}.
	 * The element type can only be of type {@link IJavaProject}, {@link IPackageFragmentRoot}, {@link IPackageFragment}, {@link IType}.
	 *
	 * Clients can extend this method (should call super) to configure additional attributes on the launch configuration working copy.
	 * @param element element to launch
	 *
	 * @return a launch configuration working copy for the given element
	 * @throws CoreException if creation failed
	 */
	protected ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element) throws CoreException {
		final String strategyName;
		final String mainTypeQualifiedName;

		switch (element.getElementType()) {
			case IJavaElement.TYPE: {
				mainTypeQualifiedName= ((IType) element).getFullyQualifiedName('.'); // don't replace, fix for binary inner types
				strategyName= element.getElementName();
			}
			break;
			
			default:
				throw new IllegalArgumentException("Invalid element type to create a launch configuration: " + element.getClass().getName()); //$NON-NLS-1$
		}

		

		ILaunchConfigurationType configType= getLaunchManager().getLaunchConfigurationType("org.jquant.launching.backtest");
		ILaunchConfigurationWorkingCopy wc= configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(strategyName));

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQualifiedName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject().getElementName());
		
		return wc;
	}
	
	/**
	 * Returns the attribute names of the attributes that are compared when looking for an existing similar launch configuration.
	 * Clients can override and replace to customize.
	 * TODO : Currency / Dates / Initial Amount  
	 * @return the attribute names of the attributes that are compared
	 */
	protected String[] getAttributeNamesToCompare() {
		return new String[] {
			IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
			IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME
		};
	}
	
	private ILaunchConfiguration findExistingLaunchConfiguration(ILaunchConfigurationWorkingCopy temporary, String mode) throws InterruptedException, CoreException {
		List<ILaunchConfiguration> candidateConfigs= findExistingLaunchConfigurations(temporary);

		// If there are no existing configs associated with the IType, create
		// one.
		// If there is exactly one config associated with the IType, return it.
		// Otherwise, if there is more than one config associated with the
		// IType, prompt the
		// user to choose one.
		int candidateCount= candidateConfigs.size();
		if (candidateCount == 0) {
			return null;
		} else if (candidateCount == 1) {
			return candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config. A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since cancelling the dialog should also cancel launching
			// anything.
			ILaunchConfiguration config= chooseConfiguration(candidateConfigs, mode);
			if (config != null) {
				return config;
			}
		}
		return null;
	}

	private List<ILaunchConfiguration> findExistingLaunchConfigurations(ILaunchConfigurationWorkingCopy temporary) throws CoreException {
		ILaunchConfigurationType configType= temporary.getType();

		ILaunchConfiguration[] configs= getLaunchManager().getLaunchConfigurations(configType);
		String[] attributeToCompare= getAttributeNamesToCompare();

		ArrayList<ILaunchConfiguration> candidateConfigs= new ArrayList<ILaunchConfiguration>(configs.length);
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration config= configs[i];
			if (hasSameAttributes(config, temporary, attributeToCompare)) {
				candidateConfigs.add(config);
			}
		}
		return candidateConfigs;
	}
	
	private static boolean hasSameAttributes(ILaunchConfiguration config1, ILaunchConfiguration config2, String[] attributeToCompare) {
		try {
			for (int i= 0; i < attributeToCompare.length; i++) {
				String val1= config1.getAttribute(attributeToCompare[i], EMPTY_STRING);
				String val2= config2.getAttribute(attributeToCompare[i], EMPTY_STRING);
				if (!val1.equals(val2)) {
					return false;
				}
			}
			return true;
		} catch (CoreException e) {
			// ignore access problems here, return false
		}
		return false;
	}
	
	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * specified launch configurations. Return the chosen config, or
	 * <code>null</code> if the user cancelled the dialog.
	 *
	 * @param configList list of {@link ILaunchConfiguration}s
	 * @param mode launch mode
	 * @return ILaunchConfiguration
	 * @throws InterruptedException if cancelled by the user
	 */
	private ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList, String mode) throws InterruptedException {
		IDebugModelPresentation labelProvider= DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle("Select a backtesting configuration");
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage("Select a backtesting configuration to debug");
		} else {
			dialog.setMessage("Select a backtesting configuration to run");
		}
		dialog.setMultipleSelection(false);
		int result= dialog.open();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		throw new InterruptedException(); // cancelled by user
	}
	
	private void showNoStrategiesFoundDialog() {
		MessageDialog.openInformation(getShell(), "Error","No Strategy found");
	}
	
	private Shell getShell() {
		return JQuantPlugin.getActiveWorkbenchShell();
	}
	
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/*
	 * Scan Phase
	 */
	//		String root = getBasePackage();
	//
	//		if (StringUtils.isNotEmpty(root) && StringUtils.isNotEmpty(strategyClassName)){
	//			throw new RuntimeException("Cannot define strategyClassName and basePackage at the same time.");
	//		}
	//		
	//		if (StringUtils.isNotEmpty(root)){
	//			/*
	//			 *  If scan mode, find all strategies
	//			 */
	//			ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
	//			provider.addIncludeFilter(new AnnotationTypeFilter(Strategy.class));
	//			
	//			Set<BeanDefinition> components = provider.findCandidateComponents(root);
	//
	//			for (BeanDefinition strategyBean : components){
	//				// FIXME : ensure strategy inherits AbstractStrategy 
	//				AbstractStrategy strategy = (AbstractStrategy) Class.forName(strategyBean.getBeanClassName()).newInstance();
	//				strategies.put(strategy.getId(), strategy);
	//			}
	//		} else {
	//			/*
	//			 *  No detection Mode 
	//			 */
	//			AbstractStrategy strategy = (AbstractStrategy) Class.forName(strategyClassName).newInstance();
	//			strategies.put(strategy.getId(), strategy);
	//		}
}

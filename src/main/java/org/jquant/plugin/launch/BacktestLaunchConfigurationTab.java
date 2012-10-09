package org.jquant.plugin.launch;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.Pair;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jquant.plugin.JQuantPlugin;

@SuppressWarnings("restriction")
public class BacktestLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

	private static final String EMPTY_STRING = "";
	private final Image fBacktestsIcon= JQuantPlugin.getImageDescriptor("money.png").createImage();
	private Label fEntryDateLabel;
	private Text fEntryDateText;
	private Label fExitDateLabel;
	private Text fExitDateText;
	private Label fCurrencyLabel;
	private Text fCurrencyText;
	private Label fAmountLabel;
	private Text fAmountText;
	private ILaunchConfiguration m_launchConfig;
	
	@Override
	public void createControl(Composite parent) {
		
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout)comp.getLayout()).verticalSpacing = 0;
		
		createGeneralParametersSection(comp);
		SWTFactory.createVerticalSpacer(comp, 1);
		createSimulationParametersSection(comp);
		setControl(comp);

	}

	
	private void createSimulationParametersSection(Composite comp) {
		Group group = SWTFactory.createGroup(comp, "Simulation Parameters", 2, 1, GridData.FILL_HORIZONTAL);
		
		List<Pair> result = getStrategyParameters();
		
		// create label -- text associated with SimulationParameters 
		
	
	}

	/**
	 * TODO : Int/Double only  
	 * @return
	 */
	private List<Pair> getStrategyParameters() {
		List<Pair> result = new ArrayList<Pair>();
		
		// 1 get Compilation Unit from the LaunchConfiguration 
		IJavaElement element = getContext();
		
		try {
			// 2 Get Type from compilation Unit
			IType type = ((ICompilationUnit)element).getTypes()[0];
			List<URL> urlList = new ArrayList<URL>();
			IClasspathEntry[] cp = type.getJavaProject().getResolvedClasspath(true);
			for (IClasspathEntry icpe : cp) {
				urlList.add(icpe.getPath().toFile().toURI().toURL());
			}
			
			String[] classPathEntries =  JavaRuntime.computeDefaultRuntimeClassPath(type.getJavaProject());
			for (int i = 0; i < classPathEntries.length; i++) {
				 String entry = classPathEntries[i];
				 IPath path = new Path(entry);
				 URL url = path.toFile().toURI().toURL();
				 urlList.add(url);
				}
			
			
			URLClassLoader classLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]));
			
			
			Class<?> clazz = classLoader.loadClass(type.getFullyQualifiedName());
			
			// 3 List all @SimulationParameters annotation in all fields 
			
			IField[] fields = type.getFields();
			
			for (IField f : fields) {
				IAnnotation ann = f.getAnnotation("Parameter");
				if (ann.exists()){
					String key = f.getElementName();
					Field champ = clazz.getDeclaredField(key);
					if (!champ.isAccessible()){
						champ.setAccessible(true);
					}
					double value = champ.getInt(clazz.newInstance());
			
					result.add(new Pair(key, value));
				}
			}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} 
		return result;
	}

	

	private void createGeneralParametersSection(Composite comp) {

		Group group = SWTFactory.createGroup(comp, "General Parameters", 2, 1, GridData.FILL_HORIZONTAL);
		
		fEntryDateLabel = new Label(group, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalIndent = 25;
		fEntryDateLabel.setLayoutData(gd);
		fEntryDateLabel.setText("Entry Date");


		fEntryDateText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fEntryDateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fEntryDateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				validatePage();
				updateLaunchConfigurationDialog();
			}

			
		});
		
		fExitDateLabel = new Label(group, SWT.NONE);
		gd = new GridData();
		gd.horizontalIndent = 25;
		fExitDateLabel.setLayoutData(gd);
		fExitDateLabel.setText("Exit Date");


		fExitDateText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fExitDateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fExitDateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				validatePage();
				updateLaunchConfigurationDialog();
			}

			
		});
		
		fCurrencyLabel = new Label(group, SWT.NONE);
		gd = new GridData();
		gd.horizontalIndent = 25;
		fCurrencyLabel.setLayoutData(gd);
		fCurrencyLabel.setText("Currency");


		fCurrencyText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fCurrencyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fCurrencyText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				validatePage();
				updateLaunchConfigurationDialog();
			}

			
		});
		
		fAmountLabel = new Label(group, SWT.NONE);
		gd = new GridData();
		gd.horizontalIndent = 25;
		fAmountLabel.setLayoutData(gd);
		fAmountLabel.setText("Initial Amount");


		fAmountText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fAmountText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAmountText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				validatePage();
				updateLaunchConfigurationDialog();
			}

			
		});
		
	}
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		validatePage();
		return getErrorMessage() == null;
	}
	
	private void validatePage() {
		// TODO Validate launch config 
		
	}

	

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IBacktestLaunchConfigurationConstants.ATTR_ENTRY_DATE, fEntryDateText.getText());
		configuration.setAttribute(IBacktestLaunchConfigurationConstants.ATTR_EXIT_DATE, fExitDateText.getText());
		configuration.setAttribute(IBacktestLaunchConfigurationConstants.ATTR_CURRENCY, fCurrencyText.getText());
		configuration.setAttribute(IBacktestLaunchConfigurationConstants.ATTR_AMOUNT, fAmountText.getText());

	}

	@Override
	public String getName() {
		return "backtest";
	}
	
	
	@Override
	public String getId() {
		return "org.jquant.plugin.backtestLaunchConfigurationTab";
	}
	

	@Override
	public Image getImage() {
		return fBacktestsIcon;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Launch configuration defaults (USD, 10000, 2000, 2010)
		
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		m_launchConfig = configuration;
		
		try {
			fEntryDateText.setText(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_ENTRY_DATE, EMPTY_STRING));
			fExitDateText.setText(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_EXIT_DATE, EMPTY_STRING));
			fCurrencyText.setText(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_CURRENCY, EMPTY_STRING));
			fAmountText.setText(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_AMOUNT, EMPTY_STRING));
		} catch (CoreException e) {
			
		}
	}
	
	/**
	 * Returns the current Java element context from which to initialize
	 * default settings, or <code>null</code> if none.
	 *
	 * @return Java element context.
	 */
	private IJavaElement getContext() {
		IWorkbenchWindow activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IJavaElement) {
						return (IJavaElement)obj;
					}
					if (obj instanceof IResource) {
						IJavaElement je = JavaCore.create((IResource)obj);
						if (je == null) {
							IProject pro = ((IResource)obj).getProject();
							je = JavaCore.create(pro);
						}
						if (je != null) {
							return je;
						}
					}
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				return (IJavaElement) input.getAdapter(IJavaElement.class);
			}
		}
		return null;
	}
}

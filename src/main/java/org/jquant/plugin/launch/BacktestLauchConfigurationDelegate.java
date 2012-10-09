package org.jquant.plugin.launch;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class BacktestLauchConfigurationDelegate extends JavaLaunchDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(MessageFormat.format("{0}...", new Object[]{configuration.getName()}), 5); //$NON-NLS-1$
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		 
		monitor.setTaskName("Verification phase");
		IJavaProject javaProject = getJavaProject(configuration);
		
		if ((javaProject == null) || !javaProject.exists()) {
			abort("Invalid Java Project", null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
		}
//		if (hasJQuantRuntime(javaProject)) {
//			abort("Can't find JQuant runtime, check your classpath.", null, -1);
//		}
		

		String strategyTypeName= getMainTypeName(configuration);
		
		String mainTypeName = verifyMainTypeName(configuration);
		IVMRunner runner = getVMRunner(configuration, mode);
	
		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName= workingDir.getAbsolutePath();
		}
		
		// Environment variables
		String[] envp= getEnvironment(configuration);

		ArrayList<String> vmArguments= new ArrayList<String>();
		ArrayList<String> programArguments= new ArrayList<String>();

		/*
		 * Add program arguments 
		 */
		programArguments.add(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_ENTRY_DATE, "1997-01-01")); //entry 
		programArguments.add(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_EXIT_DATE, "2010-01-01")); //exit
		programArguments.add(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_CURRENCY, "USD")); // currency
		programArguments.add(configuration.getAttribute(IBacktestLaunchConfigurationConstants.ATTR_AMOUNT, "10000")); //initial amount
		programArguments.add(strategyTypeName);
		
		collectExecutionArguments(configuration, vmArguments, programArguments);
		
		// Classpath
		String[] classpath= getClasspath(configuration);
		// VM-specific attributes
		Map vmAttributesMap= getVMSpecificAttributesMap(configuration);
		// Create VM config
		VMRunnerConfiguration runConfig= new VMRunnerConfiguration(mainTypeName, classpath);
		
		runConfig.setVMArguments(vmArguments.toArray(new String[vmArguments.size()]));
		runConfig.setProgramArguments(programArguments.toArray(new String[programArguments.size()]));
		runConfig.setEnvironment(envp);
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// Bootpath
		runConfig.setBootClassPath(getBootpath(configuration));

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		// done the verification phase
		monitor.worked(1);
		
		// Launch the configuration - 1 unit of work
		runner.run(runConfig, launch, monitor);
	}
	
	private void collectExecutionArguments(ILaunchConfiguration configuration, ArrayList<String> vmArguments, ArrayList<String> programArguments) throws CoreException {
		// add program & VM arguments provided by getProgramArguments and getVMArguments
		String pgmArgs= getProgramArguments(configuration);
		String vmArgs= getVMArguments(configuration);
		ExecutionArguments execArgs= new ExecutionArguments(vmArgs, pgmArgs);
		vmArguments.addAll(Arrays.asList(execArgs.getVMArgumentsArray()));
		programArguments.addAll(Arrays.asList(execArgs.getProgramArgumentsArray()));
		
	}

	
	
	@Override
	public String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return "org.jquant.Bootstrap"; //$NON-NLS-1$
	}
	
	public static boolean hasJQuantRuntime(IJavaProject project) {
		try {
			return project != null && project.findType("org.jquant.strategy.Strategy") != null;
		} catch (JavaModelException e) {
			// not available
		}
		return false;
	}

}

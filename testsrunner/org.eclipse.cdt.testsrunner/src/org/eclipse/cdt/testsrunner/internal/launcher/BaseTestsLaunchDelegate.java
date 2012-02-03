/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.TestPathUtils;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * Launch delegate implementation that redirects its queries to CDI launch C/C++
 * local application delegate, correcting the arguments attribute (to take into
 * account auto generated test module parameters) and setting up the custom
 * process factory (to handle testing process IO streams).
 */
public abstract class BaseTestsLaunchDelegate extends AbstractCLaunchDelegate {
	
    @Override
    public ILaunch getLaunch(ILaunchConfiguration config, String mode) throws CoreException {
        return getPreferredDelegate(config, mode).getLaunch(config, mode);
    }
	
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (mode.equals(ILaunchManager.RUN_MODE) || mode.equals(ILaunchManager.DEBUG_MODE)) {

			// The ATTR_PROCESS_FACTORY_ID is got from the configuration that is referred by the launch.
			// However if launch refers to the temporary working copy, the referred launch configuration 
			// will not be added to the LaunchHistory and run last configuration will not work properly.
			// So we just modify the existing configuration and revert all the changes after launch is done.
			
			ILaunchConfigurationWorkingCopy backupConfig = config.getWorkingCopy();
			try {
				// Changes launch configuration a bit and redirect it to the preferred C/C++ Application Launch delegate 
				updatedLaunchConfiguration(config);
				getPreferredDelegate(config, mode).launch(config, mode, launch, monitor);
			}
			finally {
				backupConfig.doSave();
			}
			activateTestingView();
		}
	}
	
	/**
	 * Make the necessary changes to the launch configuration before passing it
	 * to the underlying delegate. Currently, updates the program arguments with
	 * the value that was obtained from Tests Runner Plug-in.
	 * 
	 * @param config launch configuration
	 */
	private void updatedLaunchConfiguration(ILaunchConfiguration config) throws CoreException {
		ILaunchConfigurationWorkingCopy newConfig = config.getWorkingCopy();
		setProgramArguments(newConfig);
		newConfig.doSave();
	}
	
	/**
	 * Updates the program arguments with the value that was obtained from Tests
	 * Runner Plug-in.
	 * 
	 * @param config launch configuration
	 */
	private void setProgramArguments(ILaunchConfigurationWorkingCopy config) throws CoreException {
		List<?> packedTestsFilter = config.getAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_FILTER, Collections.EMPTY_LIST);
		String [][] testsFilter = TestPathUtils.unpackTestPaths(packedTestsFilter.toArray(new String[packedTestsFilter.size()]));

		// Configure test module run parameters with a Tests Runner 
		String[] params = null;
		try {
			params = getTestsRunner(config).getAdditionalLaunchParameters(testsFilter);
			
		} catch (TestingException e) {
			throw new CoreException(
					new Status(
						IStatus.ERROR, TestsRunnerPlugin.getUniqueIdentifier(),
						e.getLocalizedMessage(), null 
					)
				);
		}

		// Rewrite ATTR_PROGRAM_ARGUMENTS attribute of launch configuration
		if (params != null && params.length >= 1) {
			StringBuilder sb = new StringBuilder();
			sb.append(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
			for (String param : params) {
				sb.append(' ');
				sb.append(param);
			}
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, sb.toString());
		}
	}
	
	/**
	 * Resolves Tests Runner Plug-in interface by the value written in launch
	 * configuration.
	 * 
	 * @param config launch configuration
	 */
	private ITestsRunner getTestsRunner(ILaunchConfiguration config) throws CoreException {
		TestsRunnerInfo testsRunnerInfo = TestsRunnerPlugin.getDefault().getTestsRunnersManager().getTestsRunner(config);
		if (testsRunnerInfo == null) {
			throw new CoreException(
				new Status(
					IStatus.ERROR, TestsRunnerPlugin.getUniqueIdentifier(),
					LauncherMessages.BaseTestsLaunchDelegate_invalid_tests_runner, null 
				)
			);
		}
		ITestsRunner testsRunner = testsRunnerInfo.instantiateTestsRunner();
		if (testsRunner == null) {
			throw new CoreException(
					new Status(
						IStatus.ERROR, TestsRunnerPlugin.getUniqueIdentifier(),
						LauncherMessages.BaseTestsLaunchDelegate_tests_runner_load_failed, null 
					)
				);
		}
		return testsRunner;
	}

	/**
	 * Resolves the preferred launch delegate for the specified configuration to
	 * launch C/C++ Local Application in the specified mode. The preferred
	 * launch delegate ID is taken from <code>getPreferredDelegateId()</code>.
	 * 
	 * @param config launch configuration
	 * @param mode mode
	 * @return launch delegate
	 */
	private ILaunchConfigurationDelegate2 getPreferredDelegate(ILaunchConfiguration config, String mode) throws CoreException {
	    ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
	    ILaunchConfigurationType localCfg =
	            launchMgr.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
	    Set<String> modes = config.getModes();
	    modes.add(mode);
	    String preferredDelegateId = getPreferredDelegateId();
		for (ILaunchDelegate delegate : localCfg.getDelegates(modes)) {
			if (preferredDelegateId.equals(delegate.getId())) {
				return (ILaunchConfigurationDelegate2) delegate.getDelegate();
			}
		}
		return null;
	}	

	/**
	 * Returns the launch delegate id which should be used to redirect the
	 * launch (by default, CDI C/C++ Local Application launch delegate).
	 * 
	 * @return launch delegate ID
	 */
    public abstract String getPreferredDelegateId();
	
	/**
	 * Activates the view showing testing results.
	 */
	private void activateTestingView() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IViewPart view;
				try {
					IWorkbenchWindow activeWindow = TestsRunnerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
					view = activeWindow.getActivePage().showView("org.eclipse.cdt.testsrunner.resultsview"); //$NON-NLS-1$
					TestsRunnerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view);
				} catch (PartInitException e) {
					TestsRunnerPlugin.log(e);
				}
			}
		});
	}

	protected String getPluginID() {
		return TestsRunnerPlugin.getUniqueIdentifier();
	}
}

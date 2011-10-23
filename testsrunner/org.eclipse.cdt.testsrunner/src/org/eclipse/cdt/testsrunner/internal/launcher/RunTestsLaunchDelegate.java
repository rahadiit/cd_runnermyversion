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
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager.TestsRunnerInfo;
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
import org.eclipse.ui.PartInitException;

public class RunTestsLaunchDelegate extends AbstractCLaunchDelegate {
	
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
				// Changes launch configuration a bit and redirect it to the preferred C Application Launch delegate 
				updatedLaunchConfiguration(config);
				getPreferredDelegate(config, mode).launch(config, mode, launch, monitor);
			}
			finally {
				backupConfig.doSave();
			}
			activateTestingView();
		}
	}
	
	private void updatedLaunchConfiguration(ILaunchConfiguration config) throws CoreException {
		ILaunchConfigurationWorkingCopy newConfig = config.getWorkingCopy();
		setProgramArguments(newConfig);
		newConfig.doSave();
	}
	
	private void setProgramArguments(ILaunchConfigurationWorkingCopy config) throws CoreException {
		List<String> packedTestsFilter = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_FILTER, Collections.EMPTY_LIST);
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
	
	private ITestsRunner getTestsRunner(ILaunchConfiguration config) throws CoreException {
		TestsRunnerInfo testsRunnerInfo = TestsRunnerPlugin.getDefault().getTestsRunnersManager().getTestsRunner(config);
		if (testsRunnerInfo == null) {
			throw new CoreException(
				new Status(
					IStatus.ERROR, TestsRunnerPlugin.getUniqueIdentifier(),
					"Tests Runner is not specified or invalid", null 
				)
			);
		}
		ITestsRunner testsRunner = testsRunnerInfo.instantiateTestsRunner();
		if (testsRunner == null) {
			throw new CoreException(
					new Status(
						IStatus.ERROR, TestsRunnerPlugin.getUniqueIdentifier(),
						"Tests Runner cannot be instantiated", null 
					)
				);
		}
		return testsRunner;
	}

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

    public String getPreferredDelegateId() {
        return "org.eclipse.cdt.cdi.launch.localCLaunch";
    }
	
	private void activateTestingView() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IViewPart view;
				try {
					view = TestsRunnerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.cdt.testsrunner.resultsview");
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

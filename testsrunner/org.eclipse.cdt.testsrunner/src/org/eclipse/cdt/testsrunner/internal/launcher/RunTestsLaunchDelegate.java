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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

public class RunTestsLaunchDelegate extends AbstractCLaunchDelegate {

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			runTestModule(config, launch, monitor);
		}
// TODO: Support debug mode!
//		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
//			debugTestModule(config, launch, monitor);
//		}
	}

	private void runTestModule(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Launching Local C/C++ Test Module", 10); 
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		try {
			// Allow test module running without a project specification
			ICProject cProject = CDebugUtils.getCProject(config);
			IPath exePath = CDebugUtils.verifyProgramPath(config, cProject == null);
			String arguments[] = getProgramArgumentsArray(config);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			File wd = getWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			ArrayList<String> command = new ArrayList<String>(1 + arguments.length);
			command.add(exePath.toOSString());
			command.addAll(Arrays.asList(arguments));
			String[] commandArray = command.toArray(new String[command.size()]);
			monitor.worked(5);
			String testsRunnerId = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String)null);
			commandArray = Activator.getDefault().getTestsRunnersManager().configureLaunchParameters(testsRunnerId, commandArray);
			Process process = exec(commandArray, getEnvironment(config), wd, testsRunnerId, launch);
			monitor.worked(3);
			DebugPlugin.newProcess(launch, process, renderProcessLabel(commandArray[0]));
			
		} finally {
			monitor.done();
		}		
	}
	
	private void debugTestModule(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		IBinaryObject exeFile = null;
		monitor.beginTask("Launching Local C/C++ Test Module", 10); 
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.worked(1);
			IPath exePath = CDebugUtils.verifyProgramPath(config);
			ICProject project = CDebugUtils.verifyCProject(config);
			if (exePath != null) {
				exeFile = verifyBinary(project, exePath);
			}
			String arguments[] = getProgramArgumentsArray(config);

			// set the default source locator if required
			setDefaultSourceLocator(launch, config);

			ICDebugConfiguration debugConfig = getDebugConfig(config);
			ICDISession dsession = null;
			String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
				dsession = debugConfig.createDebugger().createDebuggerSession(launch, exeFile, new SubProgressMonitor(monitor, 8));
				try {
					try {
						ICDITarget[] dtargets = dsession.getTargets();
						for (int i = 0; i < dtargets.length; ++i) {
							ICDIRuntimeOptions opt = dtargets[i].getRuntimeOptions();
							opt.setArguments(arguments);
							File wd = getWorkingDirectory(config);
							if (wd != null) {
								opt.setWorkingDirectory(wd.getAbsolutePath());
							}
							opt.setEnvironment(getEnvironmentAsProperty(config));
						}
					} catch (CDIException e) {
						abort("Failed to set program arguments, environment or working directory.", e,
								ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
					}
					monitor.worked(1);
					boolean stopInMain = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
					String stopSymbol = null;
					if (stopInMain)
						stopSymbol = launch.getLaunchConfiguration().getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
								ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);

					ICDITarget[] targets = dsession.getTargets();
					for (int i = 0; i < targets.length; i++) {
						Process process = targets[i].getProcess();
						IProcess iprocess = null;
						if (process != null) {
							iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(exePath.toOSString()), getDefaultProcessMap());
						}
						CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i], renderTargetLabel(debugConfig),
								iprocess, exeFile, true, false, stopSymbol, true);
					}
				} catch (CoreException e) {
					try {
						dsession.terminate();
					} catch (CDIException e1) {
						// ignore
					}
					throw e;
				}
			}
		} finally {
			monitor.done();
		}		
	}
	
	/**
	 * Performs a runtime exec on the given command line in the context of the
	 * specified working directory, and returns the resulting process. If the
	 * current runtime does not support the specification of a working
	 * directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if
	 * the exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine
	 *            the command line
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @param testsRunnerId 
	 * @return the resulting process or <code>null</code> if the exec is
	 *         cancelled
	 * @see Runtime
	 */
	protected Process exec(String[] cmdLine, String[] environ, File workingDirectory, String testsRunnerId, ILaunch launch) throws CoreException {
		Process p = null;
		try {
			if (workingDirectory == null) {
				p = ProcessFactory.getFactory().exec(cmdLine, environ);
			} else {
				// NOTE: Pty should be used if possible to handle process output
				p = (PTY.isSupported())
					? ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory, new PTY())
					: ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory);
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						IViewPart view;
						try {
							view = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.cdt.testsrunner.resultsview");
							Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				// TODO: Handle incorrect tests runner somehow
				Activator.getDefault().getTestsRunnersManager().run(testsRunnerId, p.getInputStream(), launch);
			}
		} catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort("Error starting process", e, 
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working
			// directory

			IStatus status = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED,
					"Eclipse runtime does not support working directory",
					e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
					p = exec(cmdLine, environ, null, testsRunnerId, launch);
				}
			}
		}
		return p;
	}

	protected String getPluginID() {
		return Activator.getUniqueIdentifier();
	}
}

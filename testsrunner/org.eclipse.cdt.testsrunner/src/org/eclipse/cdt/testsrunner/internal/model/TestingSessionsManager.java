/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager.TestsRunnerInfo;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * TODO: Add descriptions
 * 
 */
public class TestingSessionsManager {
	
	private List<TestingSession> testingSessions = new ArrayList<TestingSession>();
	private TestsRunnersManager testsRunnersManager;
	private int activeSessionIndex;
	private List<ITestingSessionsManagerListener> listeners = new ArrayList<ITestingSessionsManagerListener>();

	public TestingSessionsManager(TestsRunnersManager testsRunnersManager) {
		this.testsRunnersManager = testsRunnersManager;
	}

	public TestingSession newSession(ILaunch launch) throws CoreException {
		// TODO: Handle incorrect tests runner somehow
		ILaunchConfiguration launchConf = launch.getLaunchConfiguration();
		String testsRunnerId = launchConf.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String)null);
		TestsRunnerInfo testsRunnerInfo = testsRunnersManager.getTestsRunner(testsRunnerId);
		// TODO: Maybe we should use not active but really the last session (case: if user switched to pre-last session and relaunch testing)
		TestingSession lastSession = testingSessions.isEmpty() ? null : testingSessions.get(activeSessionIndex);
		TestingSession newTestingSession = new TestingSession(launch, testsRunnerInfo, lastSession);
		testingSessions.add(newTestingSession);
		setActiveSession(testingSessions.size()-1);
		return newTestingSession;
	}
	
	public ITestingSession getActiveSession() {
		return testingSessions.isEmpty() ? null : testingSessions.get(activeSessionIndex);
	}
	
	public void setActiveSession(int newActiveIndex) {
		activeSessionIndex = newActiveIndex;
		// Notify listeners
		for (ITestingSessionsManagerListener listener : listeners) {
			listener.sessionActivated(testingSessions.get(activeSessionIndex));
		}
	}
	
	public void addListener(ITestingSessionsManagerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ITestingSessionsManagerListener listener) {
		listeners.remove(listener);
	}
	
}

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
import java.util.LinkedList;
import java.util.List;

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
	
	private TestsRunnersManager testsRunnersManager;
	private LinkedList<TestingSession> sessions = new LinkedList<TestingSession>();
	private TestingSession activeSession;
	private List<ITestingSessionsManagerListener> listeners = new ArrayList<ITestingSessionsManagerListener>();
	private int historySize = 10;

	public TestingSessionsManager(TestsRunnersManager testsRunnersManager) {
		this.testsRunnersManager = testsRunnersManager;
	}

	public TestingSession newSession(ILaunch launch) throws CoreException {
		// TODO: Handle incorrect tests runner somehow
		ILaunchConfiguration launchConf = launch.getLaunchConfiguration();
		String testsRunnerId = launchConf.getAttribute("org.eclipse.cdt.launch.TESTS_RUNNER", (String)null);
		TestsRunnerInfo testsRunnerInfo = testsRunnersManager.getTestsRunner(testsRunnerId);
		// TODO: Maybe we should use not active but really the last session (case: if user switched to pre-last session and relaunch testing)
		// TODO: Alternatively, we should implement smart "last" session selection here.
		TestingSession newTestingSession = new TestingSession(launch, testsRunnerInfo, activeSession);
		sessions.addFirst(newTestingSession);
		setActiveSession(newTestingSession);
		truncateHistory();
		return newTestingSession;
	}
	
	public List<? extends ITestingSession> getSessions() {
		return sessions;
	}
	
	public void setSessions(List<ITestingSession> newSessions) {
		sessions.clear();
		for (ITestingSession newSession : newSessions) {
			sessions.add((TestingSession) newSession);
		}
		truncateHistory();
	}
	
	public int getSessionsCount() {
		return sessions.size();
	}
	
	public ITestingSession getActiveSession() {
		return activeSession;
	}
	
	public void setActiveSession(ITestingSession newActiveSession) {
		if (activeSession != newActiveSession) {
			activeSession = (TestingSession) newActiveSession;
			// Notify listeners
			for (ITestingSessionsManagerListener listener : listeners) {
				listener.sessionActivated(activeSession);
			}
		}
	}
	
	public void addListener(ITestingSessionsManagerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ITestingSessionsManagerListener listener) {
		listeners.remove(listener);
	}

	public int getHistorySize() {
		return historySize;
	}

	public void setHistorySize(int historySize) {
		this.historySize = historySize;
		truncateHistory();
	}
	
	private void truncateHistory() {
		// The most frequently this method will be used to remove one element, so removeAll() is unnecessary here
		while (sessions.size() > historySize) {
			sessions.removeLast();
		}
		if (!sessions.contains(activeSession)) {
			ITestingSession newActiveSession = sessions.isEmpty() ? null : sessions.getFirst();
			setActiveSession(newActiveSession);
		}
	}
	
}

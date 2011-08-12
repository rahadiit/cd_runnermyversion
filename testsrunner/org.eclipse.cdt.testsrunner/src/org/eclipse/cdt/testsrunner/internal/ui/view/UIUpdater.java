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
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.cdt.testsrunner.internal.model.ITestingSessionsManagerListener;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.swt.widgets.Display;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class UIUpdater {
	
	private ResultsView resultsView;
	private TestsHierarchyViewer testsHierarchyViewer;
	private ProgressCountPanel progressCountPanel;
	private ITestingSessionListener sessionListener;
	private boolean autoScroll = true;
	private TestingSessionsManager sessionsManager;
	private TestingSessionsManagerListener sessionsManagerListener;
	ITestingSession testingSession;

	
	class SessionListener implements ITestingSessionListener {
		
		private void enterTestItem(final ITestItem testItem) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					resultsView.setCaption(
						testItem.getName()+" - "+TestPathUtils.getTestItemPath(testItem.getParent())
					);
					testsHierarchyViewer.getTreeViewer().update(testItem, null);
					if (autoScroll) {
						testsHierarchyViewer.getTreeViewer().reveal(testItem);
					}
				}
			});
		}
		
		public void enterTestSuite(final ITestSuite testSuite) {
			enterTestItem(testSuite);
		}
	
		public void exitTestSuite(final ITestSuite testSuite) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					testsHierarchyViewer.getTreeViewer().update(testSuite, null);
					if (autoScroll) {
						testsHierarchyViewer.getTreeViewer().setExpandedState(testSuite, false);
					}
				}
			});
		}
	
		public void enterTestCase(ITestCase testCase) {
			enterTestItem(testCase);
		}
	
		public void exitTestCase(final ITestCase testCase) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().update(testCase, null);
					// TODO: Move from here!
					resultsView.updateActionsOnTestCase(testCase.getStatus());
				}
			});
		}
	
		private void addTestItem(final ITestSuite parent, final ITestItem child) {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					testsHierarchyViewer.add(parent, child);
				}
			});
		}

		public void addTestSuite(ITestSuite parent, ITestSuite child) {
			addTestItem(parent, child);
		}
	
		public void addTestCase(ITestSuite parent, ITestCase child) {
			addTestItem(parent, child);
		}

		public void testingStarted() {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					resultsView.setCaption(testingSession.getStatusMessage());
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().refresh();
					// TODO: Move from here!
					resultsView.updateActionsBeforeRunning();
				}
			});
		}

		public void testingFinished() {
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().refresh();
					testsHierarchyViewer.getTreeViewer().expandToLevel(2);
					// TODO: Move from here!
					resultsView.updateActionsAfterRunning();
					resultsView.setCaption(testingSession.getStatusMessage());
				}
			});
		}
	}


	class TestingSessionsManagerListener implements ITestingSessionsManagerListener {
		
		public void sessionActivated(ITestingSession newTestingSession) {
			if (testingSession != newTestingSession) {
				unsubscribeFromSessionEvent();
				testingSession = newTestingSession;
				testingSession.getModelAccessor().addChangesListener(sessionListener);
				
				Display.getDefault().syncExec(new Runnable() {
					
					public void run() {
						progressCountPanel.setTestingSession(testingSession);
						testsHierarchyViewer.setTestingSession(testingSession);
						resultsView.setCaption(testingSession.getStatusMessage());
					}
				});
				// TODO: Update actions!
			}
		}
	}


	UIUpdater(ResultsView resultsView, TestsHierarchyViewer testsHierarchyViewer, ProgressCountPanel progressCountPanel, TestingSessionsManager sessionsManager) {
		this.resultsView = resultsView;
		this.testsHierarchyViewer = testsHierarchyViewer;
		this.progressCountPanel = progressCountPanel;
		this.sessionsManager = sessionsManager;
		sessionListener = new SessionListener();
		sessionsManagerListener = new TestingSessionsManagerListener();
		sessionsManager.addListener(sessionsManagerListener);
	}


	public boolean getAutoScroll() {
		return autoScroll;
	}
	
	public void setAutoScroll(boolean autoScroll) {
		this.autoScroll = autoScroll;
	}

	public void dispose() {
		unsubscribeFromSessionEvent();
		sessionsManager.removeListener(sessionsManagerListener);
	}
	
	private void unsubscribeFromSessionEvent() {
		if (testingSession != null) {
			testingSession.getModelAccessor().removeChangesListener(sessionListener);
		}
	}
	
}

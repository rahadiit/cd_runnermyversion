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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.testsrunner.internal.model.ITestingSessionsManagerListener;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

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
	UIChangesCache uiChangesCache = new UIChangesCache();
	UpdateUIJob updateUIJob = null;
	
	private static final int REFRESH_INTERVAL = 200;
	
	
	class UIChangesCache {
		
		private boolean needProgressCountPanelUpdate;
		private boolean needActionsUpdate;

		private String newViewCaption;
		private ITestItem testItemForNewViewCaption;
		
		private Map<Object, List<Object> > treeItemsToAdd = new LinkedHashMap<Object, List<Object>>();
 		private List<Object> treeItemsToUpdate = new ArrayList<Object>();
 		private Object treeItemToReveal;
		private Map<Object, Boolean> treeItemsToExpand = new LinkedHashMap<Object, Boolean>();
		
		
		UIChangesCache() {
			resetChanges();
		}

		
		public void scheduleProgressCountPanelUpdate() {
			synchronized (this) {
				needProgressCountPanelUpdate = true;
			}
		}
		
		public void scheduleActionsUpdate() {
			synchronized (this) {
				needActionsUpdate = true;
			}
		}
		
		public void scheduleViewCaptionChange(String newCaption) {
			synchronized (this) {
				newViewCaption = newCaption;
				testItemForNewViewCaption = null;
			}
		}
		
		public void scheduleViewCaptionChange(ITestItem testItem) {
			synchronized (this) {
				newViewCaption = null;
				testItemForNewViewCaption = testItem;
			}
		}
		
		public void scheduleTreeItemAdd(Object parent, Object child) {
			synchronized (this) {
				List<Object> childrenToAdd = treeItemsToAdd.get(parent);
				if (childrenToAdd == null) {
					childrenToAdd = new ArrayList<Object>();
					treeItemsToAdd.put(parent, childrenToAdd);
				}
				childrenToAdd.add(child);
			}
		}
		
		public void scheduleTreeItemUpdate(Object item) {
			synchronized (this) {
				treeItemsToUpdate.add(item);
			}
		}
		
		public void scheduleTreeItemReveal(Object item) {
			synchronized (this) {
				treeItemToReveal = item;
			}
		}
		
		public void scheduleTreeItemExpand(Object item, boolean expandedState) {
			synchronized (this) {
				treeItemsToExpand.put(item, expandedState);
			}
		}
		
		
		public void applyChanges() {
			synchronized (this) {
				TreeViewer treeViewer = testsHierarchyViewer.getTreeViewer();
				// View statistics widgets update
				if (needProgressCountPanelUpdate) {
					progressCountPanel.updateInfoFromSession();
				}
				// View actions update
				if (needActionsUpdate) {
					resultsView.updateActionsFromSession();
				}
				// View caption update
				if (newViewCaption != null) {
					resultsView.setCaption(newViewCaption);
				} else if (testItemForNewViewCaption != null) {
					resultsView.setCaption(
							testItemForNewViewCaption.getName()+" - "+
							TestPathUtils.getTestItemPath(testItemForNewViewCaption.getParent())
						);
				}
				// Tree view update
				// NOTE: Adding items should be done before update, expand, reveal etc.
				if (!treeItemsToAdd.isEmpty()) {
					// Incremental adding differs for plain and tests-in-hierarchy modes
					if (testsHierarchyViewer.showTestsHierarchy()) {
						for (Entry<Object, List<Object>> entry : treeItemsToAdd.entrySet()) {
							treeViewer.add(entry.getKey(), entry.getValue().toArray());
						}
						
					} else {
						List<Object> testCases = new ArrayList<Object>();
						for (Entry<Object, List<Object>> entry : treeItemsToAdd.entrySet()) {
							treeViewer.add(entry.getKey(), entry.getValue().toArray());
						}
						for (List<Object> children : treeItemsToAdd.values()) {
							for (Object child : children) {
								if (child instanceof ITestCase) {
									testCases.add(child);
								}
							}
						}
						Object root = treeViewer.getInput();
						treeViewer.add(root, testCases.toArray());
					}
				}
				if (!treeItemsToUpdate.isEmpty()) {
					treeViewer.update(treeItemsToUpdate.toArray(), null);
				}
				if (treeItemToReveal != null) {
					treeViewer.reveal(treeItemToReveal);
				}
				if (!treeItemsToExpand.isEmpty()) {
					for (Map.Entry<Object, Boolean> entry : treeItemsToExpand.entrySet()) {
						treeViewer.setExpandedState(entry.getKey(), entry.getValue());
					}
				}
				// All changes are applied, remove them 
				resetChangesImpl();
			}
		}

		public void resetChanges() {
			synchronized (this) {
				resetChangesImpl();
			}
		}
		
		private void resetChangesImpl() {
			needProgressCountPanelUpdate = false;
			needActionsUpdate = false;
			newViewCaption = null;
			testItemForNewViewCaption = null;
			treeItemsToAdd.clear();
			treeItemsToUpdate.clear();
			treeItemToReveal = null;
			treeItemsToExpand.clear();
		}
	}


	private class UpdateUIJob extends UIJob {

		private boolean isRunning = true;

		public UpdateUIJob() {
			super("Update C/C++ Tests Runner");
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!resultsView.isDisposed()) {
				uiChangesCache.applyChanges();
				scheduleSelf();
			}
			return Status.OK_STATUS;
		}
		
		public void scheduleSelf() {
			schedule(REFRESH_INTERVAL);
		}
		
		public void stop() {
			isRunning = false;
		}

		@Override
		public boolean shouldSchedule() {
			return isRunning;
		}

	}

	
	class SessionListener implements ITestingSessionListener {
		
		private void enterTestItem(final ITestItem testItem) {
			uiChangesCache.scheduleViewCaptionChange(testItem);
			uiChangesCache.scheduleTreeItemUpdate(testItem);
			if (autoScroll) {
				uiChangesCache.scheduleTreeItemReveal(testItem);
			}
		}
		
		public void enterTestSuite(final ITestSuite testSuite) {
			enterTestItem(testSuite);
		}
	
		public void exitTestSuite(final ITestSuite testSuite) {
			uiChangesCache.scheduleTreeItemUpdate(testSuite);
			if (autoScroll) {
				uiChangesCache.scheduleTreeItemExpand(testSuite, false);
			}
		}
	
		public void enterTestCase(ITestCase testCase) {
			enterTestItem(testCase);
		}
	
		public void exitTestCase(final ITestCase testCase) {
			uiChangesCache.scheduleActionsUpdate();
			uiChangesCache.scheduleProgressCountPanelUpdate();
			uiChangesCache.scheduleTreeItemUpdate(testCase);
		}
	
		private void addTestItem(final ITestSuite parent, final ITestItem child) {
			uiChangesCache.scheduleTreeItemAdd(parent, child);
		}

		public void addTestSuite(ITestSuite parent, ITestSuite child) {
			addTestItem(parent, child);
		}
	
		public void addTestCase(ITestSuite parent, ITestCase child) {
			addTestItem(parent, child);
		}

		public void testingStarted() {
			resultsView.updateActionsFromSession();
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					resultsView.setCaption(testingSession.getStatusMessage());
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().refresh();
				}
			});
			startUpdateUIJob();
		}

		public void testingFinished() {
			stopUpdateUIJob();
			resultsView.updateActionsFromSession();
			Display.getDefault().syncExec(new Runnable() {
				
				public void run() {
					uiChangesCache.applyChanges();
					resultsView.setCaption(testingSession.getStatusMessage());
					progressCountPanel.updateInfoFromSession();
					testsHierarchyViewer.getTreeViewer().refresh();
					testsHierarchyViewer.getTreeViewer().expandToLevel(2);
				}
			});
		}
	}


	class TestingSessionsManagerListener implements ITestingSessionsManagerListener {
		
		public void sessionActivated(ITestingSession newTestingSession) {
			if (testingSession != newTestingSession) {
				stopUpdateUIJob();
				uiChangesCache.resetChanges();
				
				unsubscribeFromSessionEvent();
				testingSession = newTestingSession;
				subscribeToSessionEvent();
				
				resultsView.updateActionsFromSession();
				Display.getDefault().syncExec(new Runnable() {
					
					public void run() {
						progressCountPanel.setTestingSession(testingSession);
						testsHierarchyViewer.setTestingSession(testingSession);
						resultsView.setCaption(testingSession != null ? testingSession.getStatusMessage() : "");
					}
				});
				if (newTestingSession != null && !newTestingSession.isFinished()) {
					startUpdateUIJob();
				}
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
	
	private void subscribeToSessionEvent() {
		if (testingSession != null) {
			testingSession.getModelAccessor().addChangesListener(sessionListener);
		}
	}

	private void unsubscribeFromSessionEvent() {
		if (testingSession != null) {
			testingSession.getModelAccessor().removeChangesListener(sessionListener);
		}
	}
	
	private void startUpdateUIJob() {
		stopUpdateUIJob();
		uiChangesCache.resetChanges();
		updateUIJob = new UpdateUIJob();
		updateUIJob.scheduleSelf();
	}
	
	private void stopUpdateUIJob() {
		if (updateUIJob != null) {
			updateUIJob.stop();
			updateUIJob = null;
		}
	}
}

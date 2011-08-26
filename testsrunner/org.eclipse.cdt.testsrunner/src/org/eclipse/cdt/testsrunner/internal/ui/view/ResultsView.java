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

import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ResultsView extends ViewPart {

	enum Orientation {
		Horizontal,
		Vertical,
		Auto,
	}
	
	private Composite parent;
	private ProgressCountPanel progressCountPanel;
	private ResultsPanel resultsPanel;
	private UIUpdater uiUpdater;
	private TestingSessionsManager sessionsManager;
	
	private Action nextAction;
	private Action previousAction;
	private Action rerunAction;
	private ToggleOrientationAction[] toggleOrientationActions;
	private Action historyAction;
	private Action showFailedOnly;
	private Action showTestsInHierarchyAction;
	private Action showTimeAction;
	private Action scrollLockAction;

	/**
	 * The current orientation preference (Horizontal, Vertical, Auto).
	 */
	private Orientation orientation = Orientation.Auto;
	
	/**
	 * The current view orientation (Horizontal or Vertical).
	 */
	private Orientation currentOrientation;
	
	private IMemento memento;
	
	// Persistence tags
	static final String TAG_ORIENTATION= "orientation"; //$NON-NLS-1$
	static final String TAG_SHOW_FAILED_ONLY= "showFailedOnly"; //$NON-NLS-1$
	static final String TAG_SHOW_TESTS_IN_HIERARCHY= "showTestsInHierarchy"; //$NON-NLS-1$
	static final String TAG_SHOW_TIME= "showTime"; //$NON-NLS-1$
	static final String TAG_SCROLL_LOCK= "scrollLock"; //$NON-NLS-1$
	static final String TAG_HISTORY_SIZE= "history_size"; //$NON-NLS-1$
	
	
	@Override
	public void createPartControl(Composite parent) {
		sessionsManager = Activator.getDefault().getTestingSessionsManager();
		IWorkbench workbench = Activator.getDefault().getWorkbench();
		Clipboard clipboard = new Clipboard(parent.getDisplay());

		this.parent = parent;
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);
		// TODO: Load 'orientation' from preferences
		currentOrientation = getActualOrientation(orientation);

		progressCountPanel = new ProgressCountPanel(parent, currentOrientation);
		resultsPanel = new ResultsPanel(parent, sessionsManager, workbench, getSite(), clipboard);
		uiUpdater = new UIUpdater(this, resultsPanel.getTestsHierarchyViewer(), progressCountPanel, sessionsManager);
		configureActionsBars(sessionsManager);
		
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				computeOrientation();
			}
		});
		
		restoreState(memento);
	}

	@Override
	public void setFocus() {
		resultsPanel.getTestsHierarchyViewer().getTreeViewer().getControl().setFocus();
	}

	private void configureActionsBars(TestingSessionsManager sessionsManager) {
		IActionBars actionBars = getViewSite().getActionBars();

		// Create common action
		toggleOrientationActions = new ToggleOrientationAction[] {
			new ToggleOrientationAction(this, Orientation.Vertical),
			new ToggleOrientationAction(this, Orientation.Horizontal),
			new ToggleOrientationAction(this, Orientation.Auto),
		};

		nextAction = new ShowNextFailureAction(resultsPanel.getTestsHierarchyViewer());
		nextAction.setEnabled(false);
		actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), nextAction);

		previousAction= new ShowPreviousFailureAction(resultsPanel.getTestsHierarchyViewer());
		previousAction.setEnabled(false);
		actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), previousAction);
		
		showFailedOnly = new ShowFailedOnlyAction(resultsPanel);
		showTestsInHierarchyAction = new ShowTestsInHierarchyAction(resultsPanel.getTestsHierarchyViewer());
		showTimeAction = new ShowTimeAction(resultsPanel.getTestsHierarchyViewer());
		scrollLockAction = new ScrollLockAction(uiUpdater);
		rerunAction = new RerunAction(sessionsManager);
		rerunAction.setEnabled(false);
		
		historyAction = new HistoryDropDownAction(sessionsManager, parent.getShell());
		
		// Configure toolbar
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(nextAction);
		toolBar.add(previousAction);
		toolBar.add(showFailedOnly);
		toolBar.add(scrollLockAction);
		toolBar.add(new Separator());
		toolBar.add(rerunAction);
		toolBar.add(historyAction);
		
		// Configure view menu
		IMenuManager viewMenu = actionBars.getMenuManager();
		viewMenu.add(showTestsInHierarchyAction);
		viewMenu.add(showTimeAction);
		viewMenu.add(new Separator());
		MenuManager layoutSubMenu = new MenuManager("&Layout");
		for (int i = 0; i < toggleOrientationActions.length; ++i) {
			layoutSubMenu.add(toggleOrientationActions[i]);
		}
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());
		viewMenu.add(showFailedOnly);
	}

	public void dispose() {
		if (uiUpdater != null) {
			uiUpdater.dispose();
		}
	}
	
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
		computeOrientation();
	}
	
	private void computeOrientation() {
		Orientation newActualOrientation = getActualOrientation(orientation);
		if (newActualOrientation != currentOrientation) {
			currentOrientation = newActualOrientation;
			progressCountPanel.setPanelOrientation(currentOrientation);
			resultsPanel.setPanelOrientation(currentOrientation);
			for (int i = 0; i < toggleOrientationActions.length; ++i) {
				toggleOrientationActions[i].setChecked(orientation == toggleOrientationActions[i].getOrientation());
			}
			parent.layout();
		}
	}
	
	private Orientation getActualOrientation(Orientation o) {
		switch (o) {
			case Horizontal:
			case Vertical:
				return o;
			case Auto:
				Point size= parent.getSize();
				return (size.x > size.y) ? Orientation.Horizontal : Orientation.Vertical;
		}
		return null;
	}

	public void updateActionsBeforeRunning() {
		previousAction.setEnabled(false);
		nextAction.setEnabled(false);
		rerunAction.setEnabled(false);
	}
	
	public void updateActionsOnTestCase(Status status) {
		// Optimization: fPreviousAction and fNextAction should be enabled or disabled together
		// so check only fNextAction.
		if (!nextAction.isEnabled() && status.isError()) {
			previousAction.setEnabled(true);
			nextAction.setEnabled(true);
		}
	}
	
	public void updateActionsAfterRunning() {
		rerunAction.setEnabled(true);
	}
	
	public void setCaption(String message) {
		setContentDescription(message);
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}
	
	private void restoreActionChecked(IMemento memento, String key, Action action) {
		Boolean checked = memento.getBoolean(key);
		if (checked != null) {
			action.setChecked(checked);
			action.run();
		}
	}

	private void restoreState(IMemento memento) {
		if (memento != null) {
			Integer orientationIndex = memento.getInteger(TAG_ORIENTATION);
			if (orientationIndex != null) {
				setOrientation(Orientation.values()[orientationIndex]);
			}
			resultsPanel.restoreState(memento);
			restoreActionChecked(memento, TAG_SHOW_FAILED_ONLY, showFailedOnly);
			restoreActionChecked(memento, TAG_SHOW_TESTS_IN_HIERARCHY, showTestsInHierarchyAction);
			restoreActionChecked(memento, TAG_SHOW_TIME, showTimeAction);
			restoreActionChecked(memento, TAG_SCROLL_LOCK, scrollLockAction);
			Integer historySize = memento.getInteger(TAG_HISTORY_SIZE);
			if (historySize != null) {
				sessionsManager.setHistorySize(historySize);
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		//Keep the old state;
		if (parent == null) {
			if (this.memento != null) { 
				memento.putMemento(this.memento);
			}
			return;
		}
		
		memento.putInteger(TAG_ORIENTATION, orientation.ordinal());
		resultsPanel.saveState(memento);
		memento.putBoolean(TAG_SHOW_FAILED_ONLY, showFailedOnly.isChecked());
		memento.putBoolean(TAG_SHOW_TESTS_IN_HIERARCHY, showTestsInHierarchyAction.isChecked());
		memento.putBoolean(TAG_SHOW_TIME, showTimeAction.isChecked());
		memento.putBoolean(TAG_SCROLL_LOCK, scrollLockAction.isChecked());
		memento.putInteger(TAG_HISTORY_SIZE, sessionsManager.getHistorySize());
	}
	
}

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

import java.util.Iterator;

import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesPanel.LevelFilter;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ResultsPanel {
	
	private SashForm sashForm;
	private MessagesPanel messagesPanel;
	private TestsHierarchyViewer testsHierarchyViewer;

	// Persistence tags
	static final String TAG_WEIGHT0 = "weight0"; //$NON-NLS-1$
	static final String TAG_WEIGHT1 = "weight1"; //$NON-NLS-1$
	static final String TAG_ERROR_FILTER_ACTION = "errorFilterAction"; //$NON-NLS-1$
	static final String TAG_WARNING_FILTER_ACTION = "warningFilterAction"; //$NON-NLS-1$
	static final String TAG_INFO_FILTER_ACTION = "infoFilterAction"; //$NON-NLS-1$

	Action errorFilterAction;
	Action warningFilterAction;
	Action infoFilterAction;


	public ResultsPanel(Composite parent, TestingSessionsManager sessionsManager, IWorkbench workbench, IWorkbenchPartSite site, Clipboard clipboard) {
		sashForm = new SashForm(parent, SWT.VERTICAL);

		// Configure tests hierarchy viewer
		// TODO: Review and simplify (if possible) this separator implementation
		ViewForm top = new ViewForm(sashForm, SWT.NONE);
		Composite empty= new Composite(top, SWT.NONE);
		empty.setLayout(new Layout() {
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(1, 1); // (0, 0) does not work with super-intelligent ViewForm
			}
			protected void layout(Composite composite, boolean flushCache) {}
		});
		top.setTopLeft(empty); // makes ViewForm draw the horizontal separator line ...
		testsHierarchyViewer = new TestsHierarchyViewer(top, site, clipboard);
		top.setContent(testsHierarchyViewer.getTreeViewer().getControl());

		// Configure test messages viewer
		ViewForm bottom= new ViewForm(sashForm, SWT.NONE);
		CLabel label = new CLabel(bottom, SWT.NONE);
		label.setText("Messages");
		bottom.setTopLeft(label);
		messagesPanel = new MessagesPanel(bottom, sessionsManager, workbench);
		ToolBar messagesToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
		ToolBarManager messagesToolBarmanager= new ToolBarManager(messagesToolBar);
		errorFilterAction = new MessageLevelFilterAction(messagesPanel, LevelFilter.Error, true);
		warningFilterAction = new MessageLevelFilterAction(messagesPanel, LevelFilter.Warning, true);
		infoFilterAction = new MessageLevelFilterAction(messagesPanel, LevelFilter.Info, false);
		messagesToolBarmanager.add(errorFilterAction);
		messagesToolBarmanager.add(warningFilterAction);
		messagesToolBarmanager.add(infoFilterAction);
		messagesToolBarmanager.update(true);
		bottom.setTopCenter(messagesToolBar);
		bottom.setContent(messagesPanel.getTableViewer().getControl());

		sashForm.setWeights(new int[]{50, 50});
	
		testsHierarchyViewer.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTestItemSelected();
			}
		});
		
		// Initialize default value
		setShowFailedOnly(false);
		
		// Data for parent (view's) layout
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	public TestsHierarchyViewer getTestsHierarchyViewer() {
		return testsHierarchyViewer;
	}

	public MessagesPanel getMessagesPanel() {
		return messagesPanel;
	}

	private void handleTestItemSelected() {
		IStructuredSelection selection = (IStructuredSelection)testsHierarchyViewer.getTreeViewer().getSelection();
		ITestItem[] testItems = new ITestItem[selection.size()];
		int index = 0;
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			testItems[index] = (ITestItem)it.next();
			++index;
		}
		messagesPanel.showItemsMessages(testItems);
	}

	public void setPanelOrientation(ResultsView.Orientation currentOrientation) {
		sashForm.setOrientation(currentOrientation == ResultsView.Orientation.Horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
	}

	public boolean getShowFailedOnly() {
		return messagesPanel.getShowFailedOnly();
	}
	
	public void setShowFailedOnly(boolean showFailedOnly) {
		testsHierarchyViewer.setShowFailedOnly(showFailedOnly);
		messagesPanel.setShowFailedOnly(showFailedOnly);
	}
	
	private void restoreActionChecked(IMemento memento, String key, Action action) {
		Boolean checked = memento.getBoolean(key);
		if (checked != null) {
			action.setChecked(checked);
			action.run();
		}
	}

	public void restoreState(IMemento memento) {
		Integer weight0 = memento.getInteger(TAG_WEIGHT0);
		Integer weight1 = memento.getInteger(TAG_WEIGHT1);
		if (weight0 != null && weight1 != null) {
			sashForm.setWeights(new int[] {weight0, weight1});
		}
		restoreActionChecked(memento, TAG_ERROR_FILTER_ACTION, errorFilterAction);
		restoreActionChecked(memento, TAG_WARNING_FILTER_ACTION, warningFilterAction);
		restoreActionChecked(memento, TAG_INFO_FILTER_ACTION, infoFilterAction);
	}

	public void saveState(IMemento memento) {
		int[] weights = sashForm.getWeights();
		memento.putInteger(TAG_WEIGHT0, weights[0]);
		memento.putInteger(TAG_WEIGHT1, weights[1]);
		memento.putBoolean(TAG_ERROR_FILTER_ACTION, errorFilterAction.isChecked());
		memento.putBoolean(TAG_WARNING_FILTER_ACTION, warningFilterAction.isChecked());
		memento.putBoolean(TAG_INFO_FILTER_ACTION, infoFilterAction.isChecked());
	}

}

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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ResultsPanel {
	
	private SashForm sashForm;
	private MessagesPanel messagesPanel;
	private TestsHierarchyViewer testsHierarchyViewer;


	public ResultsPanel(Composite parent, TestingSessionsManager sessionsManager, IWorkbench workbench) {
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
		testsHierarchyViewer = new TestsHierarchyViewer(top);
		top.setContent(testsHierarchyViewer.getTreeViewer().getControl());

		// Configure test messages viewer
		ViewForm bottom= new ViewForm(sashForm, SWT.NONE);
		CLabel label = new CLabel(bottom, SWT.NONE);
		label.setText("Messages");
		bottom.setTopLeft(label);
		messagesPanel = new MessagesPanel(bottom, sessionsManager, workbench);
		ToolBar messagesToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
		ToolBarManager messagesToolBarmanager= new ToolBarManager(messagesToolBar);
		messagesToolBarmanager.add(new MessageLevelFilterAction(messagesPanel, LevelFilter.Error, true));
		messagesToolBarmanager.add(new MessageLevelFilterAction(messagesPanel, LevelFilter.Warning, true));
		messagesToolBarmanager.add(new MessageLevelFilterAction(messagesPanel, LevelFilter.Info, false));
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
	
}

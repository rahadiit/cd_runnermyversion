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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
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
	private ModelSynchronizer modelSynchronizer;

	private ToggleOrientationAction[] toggleOrientationActions;
	
	/**
	 * The current orientation preference (Horizontal, Vertical, Auto).
	 */
	private Orientation orientation = Orientation.Auto;
	
	/**
	 * The current view orientation (Horizontal or Vertical).
	 */
	private Orientation currentOrientation;
	

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);
		// TODO: Load 'orientation' from preferences
		currentOrientation = getActualOrientation(orientation);

		progressCountPanel = new ProgressCountPanel(parent, currentOrientation);
		resultsPanel = new ResultsPanel(parent);
		modelSynchronizer = new ModelSynchronizer(resultsPanel.getTestsHierarchyViewer().getTreeViewer(), progressCountPanel);
		configureActionsBar();
		
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				computeOrientation();
			}
		});
	}

	@Override
	public void setFocus() {
		resultsPanel.getTestsHierarchyViewer().getTreeViewer().getControl().setFocus();
	}

	private void configureActionsBar() {
		// Create common action
		toggleOrientationActions = new ToggleOrientationAction[] {
			new ToggleOrientationAction(this, Orientation.Vertical),
			new ToggleOrientationAction(this, Orientation.Horizontal),
			new ToggleOrientationAction(this, Orientation.Auto),
		};

		// Configure toolbar
		IActionBars actionBars= getViewSite().getActionBars();
		IToolBarManager toolBar= actionBars.getToolBarManager();
		toolBar.add(new ScrollLockAction(modelSynchronizer));

		// Configure view menu
		IMenuManager viewMenu = actionBars.getMenuManager();
		MenuManager layoutSubMenu= new MenuManager("&Layout");
		for (int i = 0; i < toggleOrientationActions.length; ++i) {
			layoutSubMenu.add(toggleOrientationActions[i]);
		}
		viewMenu.add(layoutSubMenu);
	}

	public void dispose() {
		modelSynchronizer.dispose();
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
	
}

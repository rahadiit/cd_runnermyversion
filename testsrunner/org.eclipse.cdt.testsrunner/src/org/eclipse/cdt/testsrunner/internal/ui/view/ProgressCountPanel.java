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

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ProgressCountPanel extends Composite {

	private CounterPanel counterPanel;
	private ProgressBar progressBar;

	
	public ProgressCountPanel(Composite parent, ResultsView.Orientation currOrientation) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = (currOrientation == ResultsView.Orientation.Horizontal) ? 2 : 1;
		setLayout(layout);

		counterPanel = new CounterPanel(this);
		counterPanel.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		progressBar = new ProgressBar(this);
		progressBar.setLayoutData(
				new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		// Data for parent (view's) layout
		setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	}
	
	public CounterPanel getCounterPanel() {
		return counterPanel;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public void restart() {
		counterPanel.restart();
		progressBar.restart(0);
	}
	
	public void updateCounters(ITestItem.Status testStatus) {
		counterPanel.updateCounters(testStatus);
		progressBar.updateCounters(testStatus);
	}
	
}

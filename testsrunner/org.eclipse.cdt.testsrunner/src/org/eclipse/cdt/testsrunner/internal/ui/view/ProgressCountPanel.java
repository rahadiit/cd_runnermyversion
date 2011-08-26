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

import org.eclipse.cdt.testsrunner.model.ITestingSession;
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
	private DummyUISession dummyUISession = new DummyUISession();

	
	public ProgressCountPanel(Composite parent, ResultsView.Orientation currOrientation) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		setLayout(layout);
		setPanelOrientation(currOrientation);

		counterPanel = new CounterPanel(this, dummyUISession);
		counterPanel.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		progressBar = new ProgressBar(this, dummyUISession);
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
	
	public void setTestingSession(ITestingSession testingSession) {
		ITestingSession newSession = (testingSession != null) ? testingSession : dummyUISession;
		counterPanel.setTestingSession(newSession);
		progressBar.setTestingSession(newSession);
	}
	
	public void updateInfoFromSession() {
		counterPanel.updateInfoFromSession();
		progressBar.updateInfoFromSession();
	}

	public void setPanelOrientation(ResultsView.Orientation currentOrientation) {
		((GridLayout)getLayout()).numColumns = (currentOrientation == ResultsView.Orientation.Horizontal) ? 2 : 1;
	}
	
}

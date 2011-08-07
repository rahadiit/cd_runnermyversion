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
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import java.text.MessageFormat;


/**
 * A panel with counters for the number of Runs, Errors and Failures.
 */
// TODO: Fix description
// TODO: Refactor this class for C/C++ Unit tests
public class CounterPanel extends Composite {

	private ITestingSession testingSession;
	private Label failedCounterLabel;
	private Label abortedCounterLabel;
	private Label currentCounterLabel;
	private boolean hasSkipped;

	private final Image errorIcon= Activator.createAutoImage("ovr16/failed_counter.gif"); //$NON-NLS-1$
	private final Image failureIcon= Activator.createAutoImage("ovr16/aborted_counter.gif"); //$NON-NLS-1$

	
	public CounterPanel(Composite parent, ITestingSession testingSession) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 9;
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		currentCounterLabel = createLabel("Runs: ", null);
		failedCounterLabel = createLabel("Errors: ", errorIcon);
		abortedCounterLabel = createLabel("Failures: ", failureIcon);
		setTestingSession(testingSession);
	}

	private Label createLabel(String name, Image image) {
		Label label = new Label(this, SWT.NONE);
		if (image != null) {
			image.setBackground(label.getBackground());
			label.setImage(image);
		}
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		Label value = new Label(this, SWT.READ_ONLY);
		// TODO: Check whether this is really necessary now
		// bug: 39661 Junit test counters do not repaint correctly [JUnit]
		value.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		return value;
	}

	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
		this.hasSkipped = testingSession.getCount(ITestItem.Status.Skipped)!=0;
		updateInfoFromSession();
	}
	
	public void updateInfoFromSession() {
		setFailedCounter(testingSession.getCount(ITestItem.Status.Failed));
		setAbortedCounter(testingSession.getCount(ITestItem.Status.Aborted));
		setCurrentCounter(testingSession.getCurrentCounter(), testingSession.getCount(ITestItem.Status.Skipped));
		redraw();
	}
	
	private void setFailedCounter(int newValue) {
		failedCounterLabel.setText(Integer.toString(newValue));
	}

	private void setAbortedCounter(int newValue) {
		abortedCounterLabel.setText(Integer.toString(newValue));
	}

	private void setCurrentCounter(int currentValue, int skippedValue) {
		if (!hasSkipped && skippedValue!=0) {
			layout();
		}
		String currentCounterStr = Integer.toString(currentValue);
		String runString = (skippedValue == 0)
				? currentCounterStr
				: MessageFormat.format("{0} ({1} ignored)", new String[] { currentCounterStr, Integer.toString(skippedValue) });
		currentCounterLabel.setText(runString);
	}
	
}

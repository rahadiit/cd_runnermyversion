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
	protected Label failedCounterLabel;
	protected Label abortedCounterLabel;
	protected Label currentCounterLabel;
	protected int currentCounter;
	protected int failedCounter;
	protected int abortedCounter;
	protected int skippedCounter;

	private final Image errorIcon= Activator.createAutoImage("ovr16/failed_counter.gif"); //$NON-NLS-1$
	private final Image failureIcon= Activator.createAutoImage("ovr16/aborted_counter.gif"); //$NON-NLS-1$

	public CounterPanel(Composite parent) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 9;
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		currentCounterLabel = createLabel("Runs: ", null);
		failedCounterLabel = createLabel("Errors: ", errorIcon);
		abortedCounterLabel = createLabel("Failures: ", failureIcon);
		restart(); // Just fill labels
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

	public void restart() {
		setFailedCounter(0);
		setAbortedCounter(0);
		setCurrentCounter(0, 0);
		layout();
		redraw();
	}
	
	public void updateCounters(ITestItem.Status testStatus) {
		// Update special counters
		switch (testStatus) {
			case Skipped:
				++skippedCounter;
				// If value has been changed just now - layout again!
				if (skippedCounter == 1) {
					layout();
				}
				break;
			case Failed:
				setFailedCounter(failedCounter+1);
				break;
			case Aborted:
				setAbortedCounter(abortedCounter+1);
				break;
			case NotRun:
			case Passed:
				// Do nothing, just avoid compiler's warning
				break;
		}
		// Update current counter
		setCurrentCounter(currentCounter+1, skippedCounter);
		redraw();
	}

	private void setFailedCounter(int newValue) {
		failedCounter = newValue;
		failedCounterLabel.setText(Integer.toString(failedCounter));
	}

	private void setAbortedCounter(int newValue) {
		abortedCounter = newValue;
		abortedCounterLabel.setText(Integer.toString(abortedCounter));
	}

	private void setCurrentCounter(int currentValue, int skippedValue) {
		currentCounter = currentValue;
		skippedCounter = skippedValue;
		String currentCounterStr = Integer.toString(currentValue);
		String runString = (skippedValue == 0)
				? currentCounterStr
				: MessageFormat.format("{0} ({1} ignored)", new String[] { currentCounterStr, Integer.toString(skippedValue) });
		currentCounterLabel.setText(runString);
	}
	
}

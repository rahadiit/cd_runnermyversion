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


import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.jface.action.Action;

/**
 *  TODO: Add description here (and check other actions)!
 */
public class ShowFailedOnlyAction extends Action {

	private ResultsPanel resultsPanel;


	public ShowFailedOnlyAction(ResultsPanel resultsPanel) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.resultsPanel = resultsPanel;
		setText("Show &Failures Only");
		setToolTipText("Show &Failures Only");
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("obj16/show_failed_only.gif")); //$NON-NLS-1$
		setChecked(resultsPanel.getShowFailedOnly());
	}

	@Override
	public void run() {
		resultsPanel.setShowFailedOnly(isChecked());
	}

}

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
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * TODO: Add description
 */
public class RerunSelectedAction extends RelaunchSelectedAction {

	public RerunSelectedAction(ITestingSession testingSession, TreeViewer treeViewer) {
		super(testingSession, treeViewer);
		setText("Run");
		setToolTipText("Rerun Selected Tests"); // TODO: Add detailed tooltip
	}

	@Override
	protected String getLaunchMode() {
		return ILaunchManager.RUN_MODE;
	}
	
}


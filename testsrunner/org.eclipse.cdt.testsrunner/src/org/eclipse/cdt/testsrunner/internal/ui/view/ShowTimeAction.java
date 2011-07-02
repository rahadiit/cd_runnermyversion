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


import org.eclipse.jface.action.Action;

/**
 * Toggles tests tree hierarchy auto-scroll
 */
public class ShowTimeAction extends Action {

	private TestsHierarchyViewer testsHierarchyViewer;


	public ShowTimeAction(TestsHierarchyViewer testsHierarchyViewer) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.testsHierarchyViewer = testsHierarchyViewer;
		setText("Show Execution &Time");
		setChecked(testsHierarchyViewer.showTime());
	}

	@Override
	public void run() {
		testsHierarchyViewer.setShowTime(isChecked());
	}

}

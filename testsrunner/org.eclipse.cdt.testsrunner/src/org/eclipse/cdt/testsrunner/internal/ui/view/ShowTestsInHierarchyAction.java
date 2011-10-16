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
 * Toggles tests tree hierarchy auto-scroll
 */
public class ShowTestsInHierarchyAction extends Action {

	private TestsHierarchyViewer testsHierarchyViewer;


	public ShowTestsInHierarchyAction(TestsHierarchyViewer testsHierarchyViewer) {
		super("Show Tests in &Hierarchy", AS_CHECK_BOX); //$NON-NLS-1$
		this.testsHierarchyViewer = testsHierarchyViewer;
		setText("Show Tests in &Hierarchy");
		setChecked(testsHierarchyViewer.showTime());
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/show_tests_hierarchy.gif")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		testsHierarchyViewer.setShowTestsHierarchy(isChecked());
	}

}

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


import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.TestsHierarchyViewer;
import org.eclipse.jface.action.Action;

/**
 * Toggles tests tree hierarchy auto-scroll
 */
public class ShowNextFailureAction extends Action {

	private TestsHierarchyViewer testsHierarchyViewer;

	public ShowNextFailureAction(TestsHierarchyViewer testsHierarchyViewer) {
		super("Next Failure");
		this.testsHierarchyViewer = testsHierarchyViewer;
		setToolTipText("Next Failed Test"); // TODO: Add detailed tooltip
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/show_next.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/show_next.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/show_next.gif")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		testsHierarchyViewer.showNextFailure();
	}
}


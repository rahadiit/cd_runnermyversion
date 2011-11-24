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


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.testsrunner.internal.ui.view.TestPathUtils;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Toggles tests tree hierarchy auto-scroll
 */
public class RerunSelectedAction extends Action {

	private ITestingSession testingSession;
	private TreeViewer treeViewer;

	public RerunSelectedAction(ITestingSession testingSession, TreeViewer treeViewer) {
		super("Run");
		setToolTipText("Rerun Selected Tests"); // TODO: Add detailed tooltip
//		setDisabledImageDescriptor(Activator.getImageDescriptor("dlcl16/rerun.gif")); //$NON-NLS-1$
//		setHoverImageDescriptor(Activator.getImageDescriptor("elcl16/rerun.gif")); //$NON-NLS-1$
//		setImageDescriptor(Activator.getImageDescriptor("elcl16/rerun.gif")); //$NON-NLS-1$
		this.testingSession = testingSession;
		this.treeViewer = treeViewer;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (testingSession != null) {
			try {
				ILaunch launch = testingSession.getLaunch();
				ILaunchConfigurationWorkingCopy launchConf = launch.getLaunchConfiguration().getWorkingCopy();
				List<String> testsFilterAttr = Arrays.asList(TestPathUtils.packTestPaths(getTestItems(selection)));
				launchConf.setAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_FILTER, testsFilterAttr);
				DebugUITools.launch(launchConf, launch.getLaunchMode());
				return;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setEnabled(false);
	}
	
	private ITestItem[] getTestItems(IStructuredSelection selection) {
		ITestItem[] result = new ITestItem[selection.size()];
		int resultIndex = 0;
		for (Iterator it = selection.iterator(); it.hasNext();) {
			result[resultIndex] = (ITestItem)it.next();
			++resultIndex;
		}
		return result;
	}
	
}


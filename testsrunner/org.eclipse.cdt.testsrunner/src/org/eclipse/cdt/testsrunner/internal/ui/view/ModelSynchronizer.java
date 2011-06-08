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

import org.eclipse.cdt.testsrunner.model.IModelManagerListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ModelSynchronizer implements IModelManagerListener {
	
	private TreeViewer treeViewer;
	private ProgressCountPanel progressCountPanel;
	

	class AddTestItemRunnable implements Runnable {
		Object parent;
		Object child;

		AddTestItemRunnable(Object parent, Object child) {
			this.parent = parent;
			this.child = child;
		}

		public void run() {
			treeViewer.add(parent, child);
		}
	}

	class EnterTestItemRunnable implements Runnable {
		Object object;

		EnterTestItemRunnable(Object object) {
			this.object = object;
		}

		public void run() {
			// TODO: Update only necessary properties!
			treeViewer.update(object, null);
			treeViewer.reveal(object);
		}
	}

	class ExitTestSuiteRunnable implements Runnable {
		Object object;

		ExitTestSuiteRunnable(Object object) {
			this.object = object;
		}

		public void run() {
			// TODO: Update only necessary properties!
			treeViewer.collapseAll();
			treeViewer.update(object, null);
		}
	}

	class ExitTestCaseRunnable implements Runnable {
		ITestCase testCase;

		ExitTestCaseRunnable(ITestCase testCase) {
			this.testCase = testCase;
		}

		public void run() {
			// TODO: Update only necessary properties!
			treeViewer.update(testCase, null);
			progressCountPanel.updateCounters(testCase.getStatus());
		}
	}

	
	ModelSynchronizer(TreeViewer treeViewer, ProgressCountPanel progressCountPanel) {
		this.treeViewer = treeViewer;
		this.progressCountPanel = progressCountPanel;
	}

	public void enterTestSuite(ITestSuite testSuite) {
		Display.getDefault().syncExec(
				new EnterTestItemRunnable(testSuite));
	}

	public void exitTestSuite(ITestSuite testSuite) {
		Display.getDefault().syncExec(new ExitTestSuiteRunnable(testSuite));
	}

	public void enterTestCase(ITestCase testCase) {
		Display.getDefault().syncExec(
				new EnterTestItemRunnable(testCase));
	}

	public void exitTestCase(ITestCase testCase) {
		Display.getDefault().syncExec(
				new ExitTestCaseRunnable(testCase));
	}

	public void addTestSuite(ITestSuite parent, ITestSuite child) {
		Display.getDefault().syncExec(
				new AddTestItemRunnable(parent, child));
	}

	public void addTestCase(ITestSuite parent, ITestCase child) {
		Display.getDefault().syncExec(
				new AddTestItemRunnable(parent, child));
	}

	public void refreshModel() {
		Display.getDefault().syncExec(new Runnable() {
			
			public void run() {
				treeViewer.refresh();
			}
		});
	}

}

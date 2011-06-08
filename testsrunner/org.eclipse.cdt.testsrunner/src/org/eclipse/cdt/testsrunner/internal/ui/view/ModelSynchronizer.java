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
	

	class AddObjectRunnable implements Runnable {
		TreeViewer treeViewer;
		Object parent;
		Object child;

		AddObjectRunnable(TreeViewer treeViewer, Object parent, Object child) {
			this.treeViewer = treeViewer;
			this.parent = parent;
			this.child = child;
		}

		public void run() {
			treeViewer.add(parent, child);
		}
	}

	class ExpandRunnable implements Runnable {
		TreeViewer treeViewer;
		Object object;

		ExpandRunnable(TreeViewer treeViewer, Object object) {
			this.treeViewer = treeViewer;
			this.object = object;
		}

		public void run() {
			// TODO: Update only necessary properties!
			// treeViewer.update(testCase, null);
			treeViewer.reveal(object);
		}
	}

	class CollapseAllRunnable implements Runnable {
		TreeViewer treeViewer;

		CollapseAllRunnable(TreeViewer treeViewer) {
			this.treeViewer = treeViewer;
		}

		public void run() {
			// TODO: Update only necessary properties!
			// treeViewer.update(testSuite, null);
			treeViewer.collapseAll();
		}
	}

	class ExitTestCaseRunnable implements Runnable {
		TreeViewer treeViewer;
		ITestCase testCase;

		ExitTestCaseRunnable(TreeViewer treeViewer, ITestCase testCase) {
			this.treeViewer = treeViewer;
			this.testCase = testCase;
		}

		public void run() {
			// TODO: Update only necessary properties!
			treeViewer.update(testCase, null);
			progressCountPanel.updateCounters(testCase.getStatus());
		}
	}

	class RefreshRunnable implements Runnable {
		TreeViewer treeViewer;

		RefreshRunnable(TreeViewer treeViewer) {
			this.treeViewer = treeViewer;
		}

		public void run() {
			treeViewer.refresh();
		}
	}

	
	ModelSynchronizer(TreeViewer treeViewer, ProgressCountPanel progressCountPanel) {
		this.treeViewer = treeViewer;
		this.progressCountPanel = progressCountPanel;
	}

	public void enterTestSuite(ITestSuite testSuite) {
		Display.getDefault().syncExec(
				new ExpandRunnable(treeViewer, testSuite));
	}

	public void exitTestSuite(ITestSuite testSuite) {
		Display.getDefault().syncExec(new CollapseAllRunnable(treeViewer));
	}

	public void enterTestCase(ITestCase testCase) {
		Display.getDefault().syncExec(
				new ExpandRunnable(treeViewer, testCase));
	}

	public void exitTestCase(ITestCase testCase) {
		// treeViewer.update(testCase, null);
		Display.getDefault().syncExec(
				new ExitTestCaseRunnable(treeViewer, testCase));
	}

	public void addTestSuite(ITestSuite parent, ITestSuite child) {
		Display.getDefault().syncExec(
				new AddObjectRunnable(treeViewer, parent, child));
	}

	public void addTestCase(ITestSuite parent, ITestCase child) {
		Display.getDefault().syncExec(
				new AddObjectRunnable(treeViewer, parent, child));
	}

	public void refreshModel() {
		Display.getDefault().syncExec(new RefreshRunnable(treeViewer));
	}

}

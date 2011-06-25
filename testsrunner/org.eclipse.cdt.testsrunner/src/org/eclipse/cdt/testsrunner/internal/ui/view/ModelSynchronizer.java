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
import org.eclipse.cdt.testsrunner.internal.model.ModelManager;
import org.eclipse.cdt.testsrunner.internal.model.TestSuite;
import org.eclipse.cdt.testsrunner.model.IModelManagerListener;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ModelSynchronizer {
	
	private ResultsView resultsView;
	private TreeViewer treeViewer;
	private ProgressCountPanel progressCountPanel;
	private IModelManagerListener actualSynchronizer;

	class TestingStartedRunnable implements Runnable {
		
		private boolean restartPrevious;
		
		class TestCasesCounter implements IModelVisitor {
			
			public int result = 0;
			
			public void visit(ITestMessage testMessage) {}
			
			public void visit(ITestCase testCase) {
				++result;
			}
			
			public void visit(ITestSuite testSuite) {}
		}
		
		TestingStartedRunnable(boolean restartPrevious) {
			this.restartPrevious = restartPrevious;
		}

		public void run() {
			int totalTestsCount = 0;
			if (restartPrevious) {
				// TODO: Finish Test case counting!
				TestSuite rootTestSuite = Activator.getDefault().getModelManager().getRootSuite();
				TestCasesCounter testCasesCounter = new TestCasesCounter();
				rootTestSuite.visit(testCasesCounter);
				totalTestsCount = testCasesCounter.result;
			}
			progressCountPanel.restart(totalTestsCount);
			treeViewer.refresh();
			resultsView.resetActionsState();
		}
	}
	
	class TestingFinishedRunnable implements Runnable {

		public void run() {
			treeViewer.refresh();
			progressCountPanel.testingFinished();
			treeViewer.expandToLevel(2);
		}
	}
	
	class SilentModelSynchronizer implements IModelManagerListener {

		class CountersUpdaterRunnable implements Runnable {
			private ITestCase testCase;
	
			CountersUpdaterRunnable(ITestCase testCase) {
				this.testCase = testCase;
			}
	
			public void run() {
				// TODO: Update only necessary properties!
				progressCountPanel.updateCounters(testCase.getStatus());
				resultsView.updateActionsState(testCase.getStatus());
			}
		}
		
		public void enterTestSuite(ITestSuite testSuite) {}

		public void exitTestSuite(ITestSuite testSuite) {}

		public void enterTestCase(ITestCase testCase) {}

		public void exitTestCase(ITestCase testCase) {
			Display.getDefault().syncExec(new CountersUpdaterRunnable(testCase));
		}

		public void addTestSuite(ITestSuite parent, ITestSuite child) {}

		public void addTestCase(ITestSuite parent, ITestCase child) {}

		public void testingStarted(boolean restartPrevious) {
			Display.getDefault().syncExec(new TestingStartedRunnable(restartPrevious));
		}

		public void testingFinished() {
			Display.getDefault().syncExec(new TestingFinishedRunnable());
		}
	}
	
	
	class ScrollingModelSynchronizer implements IModelManagerListener {
		
		class AddTestItemRunnable implements Runnable {
			private Object parent;
			private Object child;
	
			AddTestItemRunnable(Object parent, Object child) {
				this.parent = parent;
				this.child = child;
			}
	
			public void run() {
				treeViewer.add(parent, child);
			}
		}
	
		class EnterTestItemRunnable implements Runnable {
			private Object object;
	
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
			private Object object;
	
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
			private ITestCase testCase;
	
			ExitTestCaseRunnable(ITestCase testCase) {
				this.testCase = testCase;
			}
	
			public void run() {
				// TODO: Update only necessary properties!
				treeViewer.update(testCase, null);
				progressCountPanel.updateCounters(testCase.getStatus());
				resultsView.updateActionsState(testCase.getStatus());
			}
		}
	
		public void enterTestSuite(ITestSuite testSuite) {
			Display.getDefault().syncExec(new EnterTestItemRunnable(testSuite));
		}
	
		public void exitTestSuite(ITestSuite testSuite) {
			Display.getDefault().syncExec(new ExitTestSuiteRunnable(testSuite));
		}
	
		public void enterTestCase(ITestCase testCase) {
			Display.getDefault().syncExec(new EnterTestItemRunnable(testCase));
		}
	
		public void exitTestCase(ITestCase testCase) {
			Display.getDefault().syncExec(new ExitTestCaseRunnable(testCase));
		}
	
		public void addTestSuite(ITestSuite parent, ITestSuite child) {
			Display.getDefault().syncExec(new AddTestItemRunnable(parent, child));
		}
	
		public void addTestCase(ITestSuite parent, ITestCase child) {
			Display.getDefault().syncExec(new AddTestItemRunnable(parent, child));
		}
	
		public void testingStarted(boolean restartPrevious) {
			Display.getDefault().syncExec(new TestingStartedRunnable(restartPrevious));
		}

		public void testingFinished() {
			Display.getDefault().syncExec(new TestingFinishedRunnable());
		}
	}

	
	ModelSynchronizer(ResultsView resultsView, TreeViewer treeViewer, ProgressCountPanel progressCountPanel) {
		this.resultsView = resultsView;
		this.treeViewer = treeViewer;
		this.progressCountPanel = progressCountPanel;
		setActualSyncronizer(new ScrollingModelSynchronizer());
	}


	public boolean getAutoScroll() {
		return (actualSynchronizer instanceof ScrollingModelSynchronizer);
	}
	
	public void setAutoScroll(boolean autoScroll) {
		if ( autoScroll != getAutoScroll() ) {
			if (autoScroll) {
				setActualSyncronizer(new ScrollingModelSynchronizer());
			} else {
				setActualSyncronizer(new SilentModelSynchronizer());
			}
		}
	}

	public void dispose() {
		setActualSyncronizer(null);
	}
	
	public void setActualSyncronizer(IModelManagerListener newActualSynchronizer) {
		ModelManager modelManager = Activator.getDefault().getModelManager();
		if (actualSynchronizer != null) {
			modelManager.removeChangesListener(actualSynchronizer);
		}
		actualSynchronizer = newActualSynchronizer;
		if (actualSynchronizer != null) {
			modelManager.addChangesListener(actualSynchronizer);
		}
	}

}

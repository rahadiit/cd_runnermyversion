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
package org.eclipse.cdt.testsrunner.internal.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 * TODO: ModelManager (case insensitive) => TestModelUpdater, "Model Manager" (ci) => ??
 */
public class TestModelManager implements ITestModelUpdater, ITestModelAccessor {

	private Stack<TestSuite> testSuitesStack = new Stack<TestSuite>();
	private TestCase currentTestCase = null;
	private Set<TestSuite> usedTestSuites = new HashSet<TestSuite>();
	
	private List<ITestingSessionListener> listeners = new ArrayList<ITestingSessionListener>();

	
	class HierarchyCopier implements IModelVisitor {

		public void visit(ITestSuite testSuite) {
			// Do not copy root test suite
			if (testSuite.getParent() != null) {
				enterTestSuite(testSuite.getName());
			}
		}

		public void leave(ITestSuite testSuite) {
			// Do not copy root test suite
			if (testSuite.getParent() != null) {
				exitTestSuite();
			}
		}

		public void visit(ITestCase testCase) {
			enterTestCase(testCase.getName());
			setTestStatus(TestCase.Status.NotRun);
		}

		public void leave(ITestCase testCase) {
			exitTestCase();
		}

		public void visit(ITestMessage testMessage) {}
		public void leave(ITestMessage testMessage) {}
	}
	

	public TestModelManager(ITestingSession previousSession) {
		testSuitesStack.push(new TestSuite("<root>", null)); //$NON-NLS-1$
		if (previousSession != null) {
			previousSession.getModelAccessor().getRootSuite().visit(new HierarchyCopier());
		}
	}

	public void testingStarted() {
		// Notify listeners
		for (ITestingSessionListener listener : listeners) {
			listener.testingStarted();
		}
	}

	public void testingFinished() {
		// Remove all NotRun-tests and not used test suites (probably they were removed from test module)
		getRootSuite().visit(new ModelVisitor() {
			
			public void visit(TestSuite testSuite) {
				for (TestSuite childTestSuite : testSuite.getTestSuites()) {
					if (!usedTestSuites.contains(childTestSuite)) {
						testSuite.removeTestSuite(childTestSuite.getName());
					}
				}
				for (TestCase testCase : testSuite.getTestCases()) {
					if (testCase.getStatus() == ITestItem.Status.NotRun) {
						testSuite.removeTestCase(testCase.getName());
					}
				}
			}

			public void visit(TestMessage testMessage) {}
			public void visit(TestCase testCase) {}
			public void leave(TestSuite testSuite) {}
			public void leave(TestCase testCase) {}
			public void leave(TestMessage testMessage) {}
		});
		usedTestSuites.clear();
		
		// Notify listeners
		for (ITestingSessionListener listener : listeners) {
			listener.testingFinished();
		}
	}
	
	public void enterTestSuite(String name) {
		TestSuite currTestSuite = testSuitesStack.peek();
		TestSuite newTestSuite = currTestSuite.getTestSuite(name);
		if (newTestSuite == null) {
			newTestSuite = new TestSuite(name, currTestSuite);
			currTestSuite.addTestSuite(newTestSuite);
			// Notify listeners
			for (ITestingSessionListener listener : listeners) {
				listener.addTestSuite(currTestSuite, newTestSuite);
			}
		}
		testSuitesStack.push(newTestSuite);
		usedTestSuites.add(newTestSuite);
		
		// Notify listeners
		for (ITestingSessionListener listener : listeners) {
			listener.enterTestSuite(newTestSuite);
		}
	}

	public void exitTestSuite() {
		exitTestCase();
		TestSuite testSuite = testSuitesStack.pop();
		// Notify listeners
		for (ITestingSessionListener listener : listeners) {
			listener.exitTestSuite(testSuite);
		}
	}

	public void enterTestCase(String name) {
		TestSuite currTestSuite = testSuitesStack.peek();
		currentTestCase = currTestSuite.getTestCase(name);
		if (currentTestCase == null) {
			currentTestCase = new TestCase(name, currTestSuite);
			currTestSuite.addTestCase(currentTestCase);
			// Notify listeners
			for (ITestingSessionListener listener : listeners) {
				listener.addTestCase(currTestSuite, currentTestCase);
			}
		}
		currentTestCase.setStatus(ITestItem.Status.Skipped);
		// Notify listeners
		for (ITestingSessionListener listener : listeners) {
			listener.enterTestCase(currentTestCase);
		}
	}


	public void setTestStatus(Status status) {
		currentTestCase.setStatus(status);
	}

	public void setTestingTime(int testingTime) {
		currentTestCase.setTestingTime(testingTime);
	}

	public void exitTestCase() {
		if (currentTestCase != null) {
			TestCase testCase = currentTestCase;
			currentTestCase = null;
			// Notify listeners
			for (ITestingSessionListener listener : listeners) {
				listener.exitTestCase(testCase);
			}
		}
	}

	public void addTestMessage(String file, int line, Level level, String text) {
		TestLocation location = (file == null || file.isEmpty() || line == 0) ? null : new TestLocation(file, line);
		currentTestCase.addTestMessage(new TestMessage(location, level, text));
	}
	
	public ITestSuite currentTestSuite() {
		return testSuitesStack.peek();
	}


	public ITestCase currentTestCase() {
		return currentTestCase;
	}

	public boolean isCurrentlyRunning(ITestItem item) {
		return (item == currentTestCase && item != null) || testSuitesStack.contains(item);
	}
	
	public TestSuite getRootSuite() {
		return testSuitesStack.firstElement();
	}

	public void addChangesListener(ITestingSessionListener listener) {
		listeners.add(listener);
	}

	public void removeChangesListener(ITestingSessionListener listener) {
		listeners.remove(listener);
	}
	
}

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

import org.eclipse.cdt.testsrunner.model.IModelManager;
import org.eclipse.cdt.testsrunner.model.IModelManagerListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 * 
 */
public class ModelManager implements IModelManager {

	private Stack<TestSuite> testSuitesStack = new Stack<TestSuite>();
	
	private TestCase currentTestCase = null;
	
	private List<IModelManagerListener> changesListeners = new ArrayList<IModelManagerListener>();
	
	private Set<TestSuite> usedTestSuites = new HashSet<TestSuite>();

	
	public ModelManager() {
		testSuitesStack.push(new TestSuite("<root>", null)); //$NON-NLS-1$
	}
	

	public void testingStarted(boolean restartPrevious) {
		if (restartPrevious) {
			// Mark all tests as NotRun before testing
			getRootSuite().visit(new ModelVisitor() {
				
				public void visit(TestMessage testMessage) {}
				
				public void visit(TestCase testCase) {
					testCase.reset();
					testCase.setStatus(TestCase.Status.NotRun);
				}
				
				public void visit(TestSuite testSuite) {}
			});
		} else {
			// Start a new session
			getRootSuite().clear();
		}
		usedTestSuites.clear();
		
		// Notify listeners
		for (IModelManagerListener listener : changesListeners) {
			listener.testingStarted(restartPrevious);
		}
	}

	public void testingFinished() {
		// Remove all NotRun-tests and not used test suites (probably they were removed from test module)
		getRootSuite().visit(new ModelVisitor() {
			
			public void visit(TestMessage testMessage) {}
			
			public void visit(TestCase testCase) {}
			
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
		});
		usedTestSuites.clear();
		
		// Notify listeners
		for (IModelManagerListener listener : changesListeners) {
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
			for (IModelManagerListener listener : changesListeners) {
				listener.addTestSuite(currTestSuite, newTestSuite);
			}
		}
		testSuitesStack.push(newTestSuite);
		usedTestSuites.add(newTestSuite);
		
		// Notify listeners
		for (IModelManagerListener listener : changesListeners) {
			listener.enterTestSuite(newTestSuite);
		}
	}

	public void exitTestSuite() {
		exitTestCase();
		TestSuite testSuite = testSuitesStack.pop();
		// Notify listeners
		for (IModelManagerListener listener : changesListeners) {
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
			for (IModelManagerListener listener : changesListeners) {
				listener.addTestCase(currTestSuite, currentTestCase);
			}
		}
		currentTestCase.setStatus(ITestItem.Status.Skipped);
		// Notify listeners
		for (IModelManagerListener listener : changesListeners) {
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
			for (IModelManagerListener listener : changesListeners) {
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
	
	public void addChangesListener(IModelManagerListener listener) {
		changesListeners.add(listener);
	}

	public void removeChangesListener(IModelManagerListener listener) {
		changesListeners.remove(listener);
	}
	
	public TestSuite getRootSuite() {
		return testSuitesStack.firstElement();
	}


}

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 */
public class TestModelManager implements ITestModelUpdater, ITestModelAccessor {

	public static final String ROOT_TEST_SUITE_NAME = "<root>";
	
	private Stack<TestSuite> testSuitesStack = new Stack<TestSuite>();
	private TestCase currentTestCase = null;
	private Map<TestItem, Integer> testSuitesIndex = new HashMap<TestItem, Integer>();
	private List<ITestingSessionListener> listeners = new ArrayList<ITestingSessionListener>();
	private boolean timeMeasurement = false;
	private long testCaseStartTime = 0;
	
	private TestSuiteInserter testSuiteInserter = new TestSuiteInserter();
	private TestCaseInserter testCaseInserter = new TestCaseInserter();

	
	private class HierarchyCopier implements IModelVisitor {

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
	

	/**
	 * Utility class: generalization of insertion algorithm for test suites and test cases
	 */
	private abstract class TestItemInserter<E extends TestItem> {

		protected abstract E checkTestItemName(TestItem item, String name);
		
		protected abstract E createTestItem(String name, TestSuite parent);
		
		protected abstract void addNewTestItem(E item);

		
		private int getLastInsertIndex(TestSuite testSuite) {
			Integer intLastInsertIndex = testSuitesIndex.get(testSuite);
			return intLastInsertIndex != null ? intLastInsertIndex : 0;
		}
		
		private void notifyAboutChildrenUpdate(ITestSuite suite) {
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.childrenUpdate(suite);
			}
		}
		
		public void insert(String name) {
			TestSuite currTestSuite = testSuitesStack.peek();
			int lastInsertIndex = getLastInsertIndex(currTestSuite);
			List<TestItem> children = currTestSuite.getChildrenList();
			E newTestItem = null;

			// Optimization: Check whether we already pointing to the test suite with required name
			try {
				newTestItem = checkTestItemName(children.get(lastInsertIndex), name);
			} catch (IndexOutOfBoundsException e) {}
			if (newTestItem != null) {
				testSuitesIndex.put(currTestSuite, lastInsertIndex+1);
			}
			
			// Check whether the suite with required name was later in the hierarchy
			if (newTestItem == null) {
				for (int childIndex = lastInsertIndex; childIndex < children.size(); childIndex++) {
					newTestItem = checkTestItemName(children.get(childIndex), name);
					if (newTestItem != null) {
						testSuitesIndex.put(currTestSuite, childIndex);
						break;
					}
				}
			}
			
			// Search in previous
			if (newTestItem == null) {
				for (int childIndex = 0; childIndex < lastInsertIndex; childIndex++) {
					newTestItem = checkTestItemName(children.get(childIndex), name);
					if (newTestItem != null) {
						children.add(lastInsertIndex, children.remove(childIndex));
						notifyAboutChildrenUpdate(currTestSuite);
						break;
					}
				}
			}
			
			// Add new
			if (newTestItem == null) {
				newTestItem = createTestItem(name, currTestSuite);
				children.add(lastInsertIndex, newTestItem);
				testSuitesIndex.put(currTestSuite, lastInsertIndex+1);
				notifyAboutChildrenUpdate(currTestSuite);
			}
			if (!testSuitesIndex.containsKey(newTestItem)) {
				testSuitesIndex.put(newTestItem, 0);
			}
			addNewTestItem(newTestItem);
		}
		
	}
	

	private class TestSuiteInserter extends TestItemInserter<TestSuite> {
		
		protected TestSuite checkTestItemName(TestItem item, String name) {
			return (item instanceof TestSuite && item.getName().equals(name)) ? (TestSuite)item : null;
		}
		
		protected TestSuite createTestItem(String name, TestSuite parent) {
			return new TestSuite(name, parent);
			
		}
		
		protected void addNewTestItem(TestSuite testSuite) {
			testSuitesStack.push(testSuite);

			// Notify listeners
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.enterTestSuite(testSuite);
			}
		}
	}


	private class TestCaseInserter extends TestItemInserter<TestCase> {
		
		protected TestCase checkTestItemName(TestItem item, String name) {
			return (item instanceof TestCase && item.getName().equals(name)) ? (TestCase)item : null;
		}
		
		protected TestCase createTestItem(String name, TestSuite parent) {
			return new TestCase(name, parent);
		}
		
		protected void addNewTestItem(TestCase testCase) {
			currentTestCase = testCase;
			testCase.setStatus(ITestItem.Status.Skipped);
			
			// Notify listeners
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.enterTestCase(testCase);
			}
		}
	}

	
	public TestModelManager(ITestSuite previousTestsHierarchy, boolean timeMeasurement) {
		testSuitesStack.push(new TestSuite(ROOT_TEST_SUITE_NAME, null)); //$NON-NLS-1$
		if (previousTestsHierarchy != null) {
			// Copy tests hierarchy
			this.timeMeasurement = false;
			previousTestsHierarchy.visit(new HierarchyCopier());
		}
		this.timeMeasurement = timeMeasurement;
		this.testSuitesIndex.clear();
	}

	public void testingStarted() {
		// Notify listeners
		for (ITestingSessionListener listener : getListenersCopy()) {
			listener.testingStarted();
		}
	}

	public void testingFinished() {
		// Remove all NotRun-tests and not used test suites (probably they were removed from test module)
		getRootSuite().visit(new ModelVisitor() {
			
			public void visit(TestSuite testSuite) {
				for (Iterator<TestItem> it = testSuite.getChildrenList().iterator(); it
						.hasNext();) {
					TestItem item = it.next();
					if ((item instanceof ITestSuite && !testSuitesIndex.containsKey(item)) ||
						(item instanceof ITestCase && item.getStatus() == ITestItem.Status.NotRun)) {
						it.remove();
					}
				}
			}

			public void visit(TestMessage testMessage) {}
			public void visit(TestCase testCase) {}
			public void leave(TestSuite testSuite) {}
			public void leave(TestCase testCase) {}
			public void leave(TestMessage testMessage) {}
		});
		testSuitesIndex.clear();
		
		// Notify listeners
		for (ITestingSessionListener listener : getListenersCopy()) {
			listener.testingFinished();
		}
	}
	
	public void enterTestSuite(String name) {
		testSuiteInserter.insert(name);
	}

	public void exitTestSuite() {
		exitTestCase();
		TestSuite testSuite = testSuitesStack.pop();
		// Notify listeners
		for (ITestingSessionListener listener : getListenersCopy()) {
			listener.exitTestSuite(testSuite);
		}
	}

	public void enterTestCase(String name) {
		testCaseInserter.insert(name);
		if (timeMeasurement) {
			testCaseStartTime = System.currentTimeMillis();
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
			// Set test execution time (if time measurement is turned on)
			if (timeMeasurement) {
				int testingTime = (int)(System.currentTimeMillis()-testCaseStartTime);
				currentTestCase.setTestingTime(currentTestCase.getTestingTime()+testingTime);
				testCaseStartTime = 0;
			}
			TestCase testCase = currentTestCase;
			currentTestCase = null;
			// Notify listeners
			for (ITestingSessionListener listener : getListenersCopy()) {
				listener.exitTestCase(testCase);
			}
		}
	}

	public void addTestMessage(String file, int line, Level level, String text) {
		TestLocation location = (file == null || file.isEmpty() || line <= 0) ? null : new TestLocation(file, line);
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
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeChangesListener(ITestingSessionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private ITestingSessionListener[] getListenersCopy() {
		synchronized (listeners) {
			return listeners.toArray(new ITestingSessionListener[listeners.size()]);
		}		
	}
	
}

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
import java.util.Map;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 * 
 */
public class TestSuite extends TestItem implements ITestSuite {

	private Map<String, TestSuite> testSuites = new HashMap<String, TestSuite>();

	private Map<String, TestCase> testCases = new HashMap<String, TestCase>();

	
	public TestSuite(String name, TestSuite parent) {
		super(name, parent);
	}

	public TestSuite getTestSuite(String name) {
		return testSuites.get(name);
	}

	public TestSuite[] getTestSuites() {
		return testSuites.values().toArray(new TestSuite[testSuites.size()]);
	}

	public TestCase getTestCase(String name) {
		return testCases.get(name);
	}

	public TestCase[] getTestCases() {
		return testCases.values().toArray(new TestCase[testCases.size()]);
	}

	public Status getStatus() {
		Status result = Status.Passed;
		for (TestSuite testSuite : testSuites.values()) {
			Status childStatus = testSuite.getStatus();
			if (result.compareTo(childStatus) < 0) {
				result = childStatus;
			}
		}
		for (TestCase testCase : testCases.values()) {
			Status childStatus = testCase.getStatus();
			if (result.compareTo(childStatus) < 0) {
				result = childStatus;
			}
		}
		return result;
	}

	public int getTestingTime() {
		int result = 0;
		for (TestSuite testSuite : testSuites.values()) {
			result += testSuite.getTestingTime();
		}
		for (TestCase testCase : testCases.values()) {
			result += testCase.getTestingTime();
		}
		return result;
	}
	
	public boolean hasChildren() {
		return !testSuites.isEmpty() || !testCases.isEmpty();
	}

	public ITestItem[] getChildren() {
		ArrayList<ITestItem> result = new ArrayList<ITestItem>(testSuites.values());
		result.addAll(testCases.values());
		return result.toArray(new ITestItem[result.size()]);
	}

	
	public void addTestSuite(TestSuite testSuite) {
		testSuites.put(testSuite.getName(), testSuite);
	}

	public void addTestCase(TestCase testCase) {
		testCases.put(testCase.getName(), testCase);
	}

	public void visit(IModelVisitor visitor) {
		visitor.visit(this);
		for (TestSuite testSuite : testSuites.values()) {
			testSuite.visit(visitor);
		}
		for (TestCase testCase : testCases.values()) {
			testCase.visit(visitor);
		}
	}

}

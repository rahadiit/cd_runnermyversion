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
package org.eclipse.cdt.testsrunner.model.internal;

import java.util.Stack;

import org.eclipse.cdt.testsrunner.model.IModelBuilder;
import org.eclipse.cdt.testsrunner.model.ITestCase.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;

/**
 * TODO: Add descriptions
 * 
 */
public class ModelBuilder implements IModelBuilder {

	private Stack<TestSuite> testSuitesStack = new Stack<TestSuite>();
	
	private TestCase currentTestCase = null;

	
	public ModelBuilder() {
		testSuitesStack.push(new TestSuite("<root>", null));
	}
	
	public void enterTestSuite(String name) {
		TestSuite currTestSuite = testSuitesStack.peek();
		TestSuite newTestSuite = currTestSuite.getTestSuite(name);
		if (newTestSuite == null) {
			newTestSuite = new TestSuite(name, currTestSuite);
			currTestSuite.addTestSuite(newTestSuite);
		}
		testSuitesStack.push(newTestSuite);
	}

	public void exitTestSuite() {
		currentTestCase = null;
		testSuitesStack.pop();
	}

	public void enterTestCase(String name) {
		TestSuite currTestSuite = testSuitesStack.peek();
		currentTestCase = currTestSuite.getTestCase(name);
		if (currentTestCase == null) {
			currentTestCase = new TestCase(name, currTestSuite);
			currTestSuite.addTestCase(currentTestCase);
		}
	}


	public void setTestStatus(Status status) {
		currentTestCase.setStatus(status);
	}

	public void setTestingTime(int testingTime) {
		currentTestCase.setTestingTime(testingTime);
	}

	public void exitTestCase() {
		currentTestCase = null;
	}

	public void addTestMessage(String file, int line, Level level, String text) {
		currentTestCase.addTestMessage(new TestMessage(new TestLocation(file, line), level, text));
	}
	
	public TestSuite getRootSuite() {
		return testSuitesStack.firstElement();
	}
	
}

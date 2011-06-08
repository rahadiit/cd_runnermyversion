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
import java.util.List;

import org.eclipse.cdt.testsrunner.model.ITestCase;

/**
 * TODO: Add descriptions
 * 
 */
public class TestCase extends TestItem implements ITestCase {

	private Status status;

	private int testingTime;

	private List<TestMessage> testMessages = new ArrayList<TestMessage>();

	
	public TestCase(String name, TestSuite parent) {
		super(name, parent);
		this.status = Status.Skipped;
		this.testingTime = 0;
	}

	public Status getStatus() {
		return status;
	}

	public int getTestingTime() {
		return testingTime;
	}

	public TestMessage[] getTestMessages() {
		return testMessages.toArray(new TestMessage[testMessages.size()]);
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTestingTime(int testingTime) {
		this.testingTime = testingTime;
	}

	public void addTestMessage(TestMessage testMessage) {
		testMessages.add(testMessage);
	}

}

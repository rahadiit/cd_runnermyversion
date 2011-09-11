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
package org.eclipse.cdt.testsrunner.testsrunners;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 */
@SuppressWarnings("nls")
public class MockTestModelUpdater implements ITestModelUpdater {

	private class MethodInfo {
		
		private String methodName;
		private Object[] args;
		
		MethodInfo(String methodName, Object[] args) {
			this.methodName = methodName;
			this.args = args;
		}
		
		private String genArgs(String methodName, Object[] args) {
			StringBuilder sb = new StringBuilder();
			sb.append(methodName);
			sb.append("(");
			boolean needDelimited = false;
			for (Object arg : args) {
				if (needDelimited) {
					sb.append(", ");
				} else {
					needDelimited = true;
				}
				if (arg != null) {
					sb.append('"');
					sb.append(arg.toString());
					sb.append('"');
				} else {
					sb.append("null");
				}
			}
			sb.append(")");
			return sb.toString();
		}

		public void check(String methodName, Object[] args) {
			if (!this.methodName.equals(methodName)) {
				Assert.failNotEquals("Unexpected method call. ", 
						genArgs(this.methodName, this.args), genArgs(methodName, args));
			}
			boolean compareFailed = (this.args.length != args.length);
			if (!compareFailed) {
				for (int i = 0; i < args.length; i++) {
					if (this.args[i] == null) {
						if (args[i] != null) {
							compareFailed = true;
							break;
						}
					} else if (!this.args[i].equals(args[i])) {
						compareFailed = true;
						break;
					}
				}
			}
			if (compareFailed) {
				Assert.failNotEquals("Unexpected parameters of method "+this.methodName+"(). ", 
						genArgs(this.methodName, this.args), genArgs(methodName, args));
			}
		}
	}
	
	
	private LinkedList<MethodInfo> methodCalls = new LinkedList<MethodInfo>();
	private boolean replayMode = false;
	private Set<String> skippedMethods = new HashSet<String>();
	
	
	public void enterTestSuite(String name) {
		genericImpl("enterTestSuite", name);
	}

	public void exitTestSuite() {
		genericImpl("exitTestSuite");
	}

	public void enterTestCase(String name) {
		genericImpl("enterTestCase", name);
	}


	public void setTestStatus(Status status) {
		genericImpl("setTestStatus", status);
	}

	public void setTestingTime(int testingTime) {
		genericImpl("setTestingTime", testingTime);
	}

	public void exitTestCase() {
		genericImpl("exitTestCase");
	}

	public void addTestMessage(String file, int line, Level level, String text) {
		genericImpl("addTestMessage", file, line, level, text);
	}
	
	public ITestSuite currentTestSuite() {
		return null;
	}

	public ITestCase currentTestCase() {
		return null;
	}
	
	public void skipCalls(String methodName) {
		skippedMethods.add(methodName);
	}
	
	public void replay() {
		replayMode = true;
	}
	
	private void genericImpl(String methodName, Object... args) {
		if (!skippedMethods.contains(methodName)) {
			if (replayMode) {
				if (methodCalls.isEmpty()) {
					Assert.fail("Unexpected method call "+methodName+"()");
				} else {
					methodCalls.pollFirst().check(methodName, args);
				}
			} else {
				methodCalls.addLast(new MethodInfo(methodName, args));
			}
		}
	}

}

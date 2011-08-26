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
package org.eclipse.cdt.testsrunner.model;

/**
 * TODO: Add descriptions
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestModelUpdater {

	public void enterTestSuite(String name);
	
	public void exitTestSuite();
	

	public void enterTestCase(String name);
	
	public void setTestStatus(ITestItem.Status status);
	
	public void setTestingTime(int testingTime);
	
	public void exitTestCase();

	
	public void addTestMessage(String file, int line, ITestMessage.Level level, String text);
	
	
	public ITestSuite currentTestSuite();
	
	public ITestCase currentTestCase();
	
}

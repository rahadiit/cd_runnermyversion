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
public interface ITestingSessionListener {

	public void enterTestSuite(ITestSuite testSuite);
	
	public void exitTestSuite(ITestSuite testSuite);
	
	public void enterTestCase(ITestCase testCase);
	
	public void exitTestCase(ITestCase testCase);

	public void childrenUpdate(ITestSuite testSuite);
	
	public void testingStarted();

	public void testingFinished();
	
}

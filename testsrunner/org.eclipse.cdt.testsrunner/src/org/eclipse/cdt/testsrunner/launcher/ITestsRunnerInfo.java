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
package org.eclipse.cdt.testsrunner.launcher;

/**
 * Describes the Tests Runner Plug-in, its requirements and features provided.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestsRunnerInfo {
	
	/**
	 * Returns the user readable name of the Tests Runner Plug-in.
	 *
	 * @return readable name
	 */
	public String getName();

	/**
	 * Returns the unique ID of the Tests Runner Plug-in.
	 *
	 * @return unique id
	 */
	public String getId();

	/**
	 * Returns the short description of the Tests Runner Plug-in.
	 *
	 * @return short description
	 */
	public String getDescription();

	/**
	 * Returns whether Tests Runner Plug-in allows to add a filter for running a
	 * few custom test cases or test suites at a time (e.g. Google Test and Qt
	 * Test allow it, but Boost.Test doesn't).
	 * 
	 * @return whether multiple filter is supported
	 */
	public boolean isAllowedMultipleTestFilter();
	
	/**
	 * Returns whether Tests Runner Plug-in requires to handle standard output
	 * stream of the testing process.
	 * 
	 * @return whether output stream is required
	 */
	public boolean isOutputStreamRequired();
	
	/**
	 * Returns whether Tests Runner Plug-in requires to handle standard error
	 * stream of the testing process.
	 * 
	 * @return whether error stream is required
	 */
	public boolean isErrorStreamRequired();
	
}

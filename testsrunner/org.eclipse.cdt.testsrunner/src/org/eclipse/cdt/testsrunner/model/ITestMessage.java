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
 * Describes the message that was produced during the testing process.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestMessage {

	/**
	 * The level of the test message.
	 */
	public enum Level {
		Info,
		Message,
		Warning,
		Error,
		FatalError,
		Exception,
	}
	
	/**
	 * Returns the location of the test message.
	 * 
	 * @return message location
	 */
	public ITestLocation getLocation();

	/**
	 * Returns the level of the test message.
	 * 
	 * @return message level
	 */
	public Level getLevel();

	/**
	 * Returns the text of the test message.
	 * 
	 * @return message text
	 */
	public String getText();

	/**
	 * Visitor pattern support for the tests hierarchy.
	 * 
	 * @param visitor - any object that supports visitor interface
	 */
	public void visit(IModelVisitor visitor);

}

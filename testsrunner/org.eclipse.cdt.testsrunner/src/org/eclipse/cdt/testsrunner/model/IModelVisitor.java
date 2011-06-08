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
 */
public interface IModelVisitor {

	void visit(ITestSuite testSuite);
	
	void visit(ITestCase testCase);
	
	void visit(ITestMessage testMessage);
	
}

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

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
// TODO: Need this (and all visit() methods)?
public abstract class ModelVisitor implements IModelVisitor {

	public void visit(ITestSuite testSuite) {
		visit((TestSuite)testSuite);
	}
	
	public void visit(ITestCase testCase) {
		visit((TestCase)testCase);
	}
	
	public void visit(ITestMessage testMessage) {
		visit((TestMessage)testMessage);
	}

	public void leave(ITestSuite testSuite) {
		visit((TestSuite)testSuite);
	}
	
	public void leave(ITestCase testCase) {
		visit((TestCase)testCase);
	}
	
	public void leave(ITestMessage testMessage) {
		visit((TestMessage)testMessage);
	}

	public abstract void visit(TestSuite testSuite);
	
	public abstract void visit(TestCase testCase);
	
	public abstract void visit(TestMessage testMessage);

	public abstract void leave(TestSuite testSuite);
	
	public abstract void leave(TestCase testCase);
	
	public abstract void leave(TestMessage testMessage);
	
}

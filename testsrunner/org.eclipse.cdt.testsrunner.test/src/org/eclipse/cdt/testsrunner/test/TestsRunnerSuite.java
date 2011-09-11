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
package org.eclipse.cdt.testsrunner.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.testsrunner.testsrunners.BoostTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.QtTestCase;


public class TestsRunnerSuite extends TestSuite {
	public TestsRunnerSuite() {
	}

	public TestsRunnerSuite(Class<? extends TestCase> theClass, String name) {
		super(theClass, name);
	}

	public TestsRunnerSuite(Class<? extends TestCase> theClass) {
		super(theClass);
	}

	public TestsRunnerSuite(String name) {
		super(name);
	}

	public static Test suite() {
		final TestsRunnerSuite suite = new TestsRunnerSuite();
		// tests runners
		suite.addTestSuite(BoostTestCase.class);
		suite.addTestSuite(QtTestCase.class);
		return suite;
	}
}

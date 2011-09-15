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

import org.eclipse.cdt.testsrunner.model.ITestItem;

/**
 * Common implementation for the structural item of test hierarchy (test suite
 * or test case).
 */
public abstract class TestItem implements ITestItem {

	/** Test item has no children by default. */
	private static final ITestItem[] NO_CHILDREN = new ITestItem[0];

	/** Test item name. */
	private final String name;
	
	/** Item parent test suite. May be <code>null</code> for root test suite. */
	private TestSuite parent;

	
	public TestItem(String name, TestSuite parent) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public TestSuite getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return false;
	}

	public ITestItem[] getChildren() {
		return NO_CHILDREN;
	}

}

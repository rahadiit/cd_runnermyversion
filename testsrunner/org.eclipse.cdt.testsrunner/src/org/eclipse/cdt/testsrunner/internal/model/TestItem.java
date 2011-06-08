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
 * TODO: Add descriptions
 * 
 */
public abstract class TestItem implements ITestItem {

	private final ITestItem[] NO_CHILDREN= new ITestItem[0];

	private final String name;
	
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

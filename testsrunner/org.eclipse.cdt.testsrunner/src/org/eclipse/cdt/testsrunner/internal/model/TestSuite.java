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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * TODO: Add descriptions
 * 
 */
public class TestSuite extends TestItem implements ITestSuite {

	private List<TestItem> children = new ArrayList<TestItem>();
	
	
	public TestSuite(String name, TestSuite parent) {
		super(name, parent);
	}

	public Status getStatus() {
		Status result = Status.NotRun;
		for (TestItem testItem : children) {
			Status childStatus = testItem.getStatus();
			if (result.compareTo(childStatus) < 0) {
				result = childStatus;
			}
		}
		return result;
	}

	public int getTestingTime() {
		int result = 0;
		for (TestItem testItem : children) {
			result += testItem.getTestingTime();
		}
		return result;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public TestItem[] getChildren() {
		return children.toArray(new TestItem[children.size()]);
	}

	public void visit(IModelVisitor visitor) {
		visitor.visit(this);
		for (TestItem testItem : children) {
			testItem.visit(visitor);
		}
		visitor.leave(this);
	}

	public List<TestItem> getChildrenList() {
		return children;
	}
	
}

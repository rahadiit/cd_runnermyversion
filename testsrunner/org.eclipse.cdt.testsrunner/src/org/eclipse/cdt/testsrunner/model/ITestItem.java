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
public interface ITestItem {

	// NOTE: Order of values is significant (cause enum values comparison is necessary)
	public enum Status {
		NotRun,
		Skipped,
		Passed,
		Failed,
		Aborted;

		public boolean isError() {
			return (this == Failed) || (this == Aborted);
		}
	}
	
	public String getName();
	
	public Status getStatus();

	public int getTestingTime();

	public ITestSuite getParent();
	
	public boolean hasChildren();

	public ITestItem[] getChildren();
	
	public void visit(IModelVisitor visitor);

}

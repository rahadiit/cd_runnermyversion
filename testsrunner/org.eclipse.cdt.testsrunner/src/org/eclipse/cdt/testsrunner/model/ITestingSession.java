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

import org.eclipse.debug.core.ILaunch;


/**
 * TODO: Add descriptions
 * 
 */
public interface ITestingSession {

	public int getCurrentCounter();

	public int getTotalCounter();
	
	public int getCount(ITestItem.Status status);

	public boolean hasErrors();

	public boolean wasStopped();

	public boolean isFinished();
	
	public ITestModelAccessor getModelAccessor();

	public ILaunch getLaunch();
	
	public ITestsRunnerInfo getTestsRunnerInfo();

	public String getStatusMessage();

	public String getName();

	public void stop();
	
}
	

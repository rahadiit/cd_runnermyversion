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
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestsRunnerInfo;
import org.eclipse.debug.core.ILaunch;

/**
 * TODO: Add descriptions
 * 
 */
public class DummyUISession implements ITestingSession {

	public int getCurrentCounter() {
		return 0;
	}

	public int getTotalCounter() {
		return 0;
	}
	
	public int getCount(ITestItem.Status status) {
		return 0;
	}

	public boolean hasErrors() {
		return false;
	}

	public boolean wasStopped() {
		return false;
	}

	public boolean isFinished() {
		return false;
	}

	public ITestModelAccessor getModelAccessor() {
		return null;
	}

	public ILaunch getLaunch() {
		return null;
	}

	public ITestsRunnerInfo getTestsRunnerInfo() {
		return null;
	}

	public String getStatusMessage() {
		return ""; //$NON-NLS-1$
	}

}

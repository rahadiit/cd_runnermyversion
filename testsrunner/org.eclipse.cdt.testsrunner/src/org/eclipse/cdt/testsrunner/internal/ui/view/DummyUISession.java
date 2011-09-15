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

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerInfo;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.debug.core.ILaunch;

/**
 * Represents a simple testing session which is used for UI when there is no
 * "real" testing sessions to show (e.g. when there was no launched testing
 * session or when all of them were cleared).
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

	public String getName() {
		return "<dummy>"; //$NON-NLS-1$
	}

	public void stop() {
	}

}

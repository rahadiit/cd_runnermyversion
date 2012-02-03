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
package org.eclipse.cdt.testsrunner.internal.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Collects the data from the Tests Runner Plug-in extension points and provides
 * the convenient access to it.
 */
public class TestsRunnersManager {
	
	/** Tests Runner Plug-ins extension point ID. */
	private static final String TESTS_RUNNER_EXTENSION_POINT_ID = "org.eclipse.cdt.testsrunner.TestsRunner"; //$NON-NLS-1$

	/** Tests Runner Plug-ins information collection. */
	private TestsRunnerInfo[] testsRunners = null;

	
	/**
	 * Provides access to information about all registered Tests Runner
	 * Plug-ins.
	 * 
	 * @return array of tests runner plug-ins descriptors
	 */
	public TestsRunnerInfo[] getTestsRunnersInfo() {
		if (testsRunners == null) {
			// Initialize tests runners info
			List<TestsRunnerInfo> testsRunnersList = new ArrayList<TestsRunnerInfo>();
			for (IConfigurationElement element : Platform.getExtensionRegistry().getConfigurationElementsFor(TESTS_RUNNER_EXTENSION_POINT_ID)) {
				testsRunnersList.add(new TestsRunnerInfo(element));
			}
			testsRunners = testsRunnersList.toArray(new TestsRunnerInfo[testsRunnersList.size()]);
		}
		return testsRunners;
	}

	/**
	 * Provides access to information about Tests Runner Plug-in referred in the
	 * specified launch configuration.
	 * 
	 * @return tests runner plug-in descriptor
	 */
	public TestsRunnerInfo getTestsRunner(ILaunchConfiguration launchConf) throws CoreException {
		String testsRunnerId = launchConf.getAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String)null);
		return getTestsRunner(testsRunnerId);
	}
	
	/**
	 * Provides access to information about Tests Runner Plug-in with the
	 * specified ID.
	 * 
	 * @return tests runner plug-in descriptor
	 */
	private TestsRunnerInfo getTestsRunner(String testsRunnerId) {
		if (testsRunnerId!=null) {
			for (TestsRunnerInfo testsRunner : getTestsRunnersInfo()) {
				if (testsRunner.getId().equals(testsRunnerId)) {
					return testsRunner;
				}
			}
		}
		return null;
	}
	
}

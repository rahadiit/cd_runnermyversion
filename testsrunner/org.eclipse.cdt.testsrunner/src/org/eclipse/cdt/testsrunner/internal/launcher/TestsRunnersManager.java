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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * TODO: Add descriptions
 * 
 */
public class TestsRunnersManager {
	
	private static final String TESTS_RUNNER_EXTENSION_POINT_ID = "org.eclipse.cdt.testsrunner.TestsRunner"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String TESTS_RUNNER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	public class TestsRunnerInfo {
		private final String name;
		private ITestsRunner testsRunner;
		
		public TestsRunnerInfo(String name, ITestsRunner testsRunner) {
			this.name = name;
			this.testsRunner = testsRunner;
		}

		public String getName() {
			return name;
		}

		public ITestsRunner getTestsRunner() {
			return testsRunner;
		}
	}

	private List<TestsRunnerInfo> testsRunners = null;

	public TestsRunnersManager() {
	}

	private void loadTestsRunners() {
		testsRunners = new ArrayList<TestsRunnerInfo>();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TESTS_RUNNER_EXTENSION_POINT_ID);
		try {
			for (IConfigurationElement element : config) {
				final String name = element.getAttribute(TESTS_RUNNER_NAME_ATTRIBUTE);
				final Object object = element.createExecutableExtension(TESTS_RUNNER_CLASS_ATTRIBUTE);
				if (object instanceof ITestsRunner) {
					testsRunners.add(new TestsRunnerInfo(name, (ITestsRunner) object));
				}
			}
		} catch (CoreException ex) {
			Activator.log(ex);
		}
	}
	
	public TestsRunnerInfo[] getTestsRunnersInfo() {
		if (testsRunners == null) {
			loadTestsRunners();
		}
		return testsRunners.toArray(new TestsRunnerInfo[testsRunners.size()]);
	}

	public void run(InputStream inputStream) {
		// TODO: Review test runners selection!!!
		TestsRunnerInfo testsRunner = getTestsRunnersInfo()[0];
		testsRunner.testsRunner.run(Activator.getDefault().getModelManager(), inputStream);
	}

}

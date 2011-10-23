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
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.model.TestingSession;
import org.eclipse.cdt.testsrunner.model.ITestsRunnerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;

/**
 * TODO: Add descriptions
 * 
 */
public class TestingProcessFactory implements IProcessFactory {

	class ThreadRunnable implements Runnable {

		private TestingSession testingSession;
		private InputStream iStream;
		private ProcessWrapper processWrapper;
		
		ThreadRunnable(TestingSession testingSession, InputStream iStream, ProcessWrapper processWrapper) {
			this.testingSession = testingSession;
			this.iStream = iStream;
			this.processWrapper = processWrapper;
		}
		
		public void run() {
			try {
				testingSession.run(iStream);
			}
			finally {
				processWrapper.allowStreamsClosing();
			}
		}
	}
	
	
	public IProcess newProcess(ILaunch launch, Process process, String label, Map attributes) {
		
		try {
			TestingSession testingSession = TestsRunnerPlugin.getDefault().getTestingSessionsManager().newSession(launch);
			ITestsRunnerInfo testsRunnerInfo = testingSession.getTestsRunnerInfo();
			InputStream iStream = 
					testsRunnerInfo.isOutputStreamRequired() ? process.getInputStream() :
					testsRunnerInfo.isErrorStreamRequired() ? process.getErrorStream() : null;
			ProcessWrapper processWrapper = new ProcessWrapper(process, testsRunnerInfo.isOutputStreamRequired(), testsRunnerInfo.isErrorStreamRequired());
			Thread t = new Thread(new ThreadRunnable(testingSession, iStream, processWrapper));
			t.start();
			// For CDI we can just create RuntimeProcess, but for DSF InferiorRuntimeProcess should be created
			return new InferiorRuntimeProcess(launch, processWrapper, label, attributes);
			
		} catch (CoreException e) {
			TestsRunnerPlugin.log(e);
		}
		return null;
	}

}

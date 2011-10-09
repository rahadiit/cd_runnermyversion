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

import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager.TestsRunnerInfo;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;

/**
 * TODO: Add descriptions
 * 
 */
public class TestingSession implements ITestingSession {

	private ILaunch launch;
	private TestsRunnerInfo testsRunnerInfo;
	private ITestsRunner testsRunner;
	private TestModelManager modelManager;
	private int totalCounter = -1;
	private int currentCounter = 0;
	private Map<ITestItem.Status, Integer> statusCounters = new EnumMap<ITestItem.Status, Integer>(ITestItem.Status.class);
	private boolean hasErrors = false;
	private boolean wasStopped = false;
	private boolean finished = false;
	private String statusMessage = "Starting...";
	private long startTime;
	
	
	class TestCasesCounter implements IModelVisitor {
		
		public int result = 0;
		
		public void visit(ITestCase testCase) {
			++result;
		}
		
		public void visit(ITestSuite testSuite) {}
		public void visit(ITestMessage testMessage) {}
		public void leave(ITestSuite testSuite) {}
		public void leave(ITestCase testCase) {}
		public void leave(ITestMessage testMessage) {}
	}

	
	class TestingTimeCounter implements IModelVisitor {
		
		public int result = 0;
		
		public void visit(ITestCase testCase) {
			result += testCase.getTestingTime();
		}
		
		public void visit(ITestSuite testSuite) {}
		public void visit(ITestMessage testMessage) {}
		public void leave(ITestSuite testSuite) {}
		public void leave(ITestCase testCase) {}
		public void leave(ITestMessage testMessage) {}
	}

	
	public TestingSession(ILaunch launch, TestsRunnerInfo testsRunnerInfo, TestingSession previousSession) {
		this.launch = launch;
		this.testsRunnerInfo = testsRunnerInfo;
		this.testsRunner = testsRunnerInfo.instantiateTestsRunner();
		this.startTime = System.currentTimeMillis();
		// Check whether we can rely on the tests hierarchy of previous testing session:
		//   - it should be for the same launch configuration (should have the same parameters)
		//   - should be already terminated (to have complete tests hierarchy structure)
		//   - should not be stopped by user (the same as terminated)
		//   - should have the same tests runner
		// Certainly, it is not full list of requirements, but it covers the most common cases
		if (previousSession != null) {
			if (!launch.getLaunchConfiguration().equals(previousSession.launch.getLaunchConfiguration())
				|| !previousSession.isFinished()
				|| previousSession.wasStopped()
				|| !previousSession.getTestsRunnerInfo().getName().equals(getTestsRunnerInfo().getName())) {
				previousSession = null;
			}
		}
		// Calculate approximate tests count by the previous similar testing session (if available)
		if (previousSession!=null) {
			TestCasesCounter testCasesCounter = new TestCasesCounter();
			previousSession.getModelAccessor().getRootSuite().visit(testCasesCounter);
			totalCounter = testCasesCounter.result;
		}
		this.modelManager = new TestModelManager(previousSession, testsRunnerInfo.isAllowedTestingTimeMeasurement());
		this.modelManager.addChangesListener(new ITestingSessionListener() {
			
			public void testingStarted() {}
			
			public void testingFinished() {
				// This is necessary if totalCounter was -1 (tests count was unknown)
				// or if tests count was estimated not accurately
				totalCounter = currentCounter;
			}
			
			public void exitTestSuite(ITestSuite testSuite) {}
			
			public void exitTestCase(ITestCase testCase) {
				// Update testing session info (counters, flags)
				Status testStatus = testCase.getStatus();
				statusCounters.put(testStatus, getCount(testStatus)+1);
				++currentCounter;
				if (testStatus.isError())
					hasErrors = true;
			}
			
			public void enterTestSuite(ITestSuite testSuite) {}
			
			public void enterTestCase(ITestCase testCase) {}
			
			public void addTestSuite(ITestSuite parent, ITestSuite child) {}
			
			public void addTestCase(ITestSuite parent, ITestCase child) {}
		});
	}

	public String [] configureLaunchParameters(String[] parameters, String [][] testsFilter) throws TestingException {
		try {
			return testsRunner.configureLaunchParameters(parameters, testsFilter);
			
		} catch (TestingException e) {
			statusMessage = e.getLocalizedMessage();
			hasErrors = true;
			finished = true;
			modelManager.testingFinished();
			throw e;
		}
	}

	public void run(InputStream inputStream) {
		modelManager.testingStarted();
		try {
			testsRunner.run(modelManager, inputStream);
			TestingTimeCounter testingTimeCounter = new TestingTimeCounter();
			// If testing session was stopped, the status is set in stop()
			if (!wasStopped()) {
				getModelAccessor().getRootSuite().visit(testingTimeCounter);
				statusMessage = MessageFormat.format("Finished after {0} seconds", testingTimeCounter.result/1000.0);
			}
		} catch (TestingException e) {
			// If testing session was stopped, the status is set in stop()
			if (!wasStopped()) {
				statusMessage = e.getLocalizedMessage();
				hasErrors = true;
			}
		}
		finished = true;
		modelManager.testingFinished();
	}

	public int getCurrentCounter() {
		return currentCounter;
	}

	/**
	 * NOTE: Total counter may be -1 if total tests count is unknown
	 */
	public int getTotalCounter() {
		return totalCounter;
	}
	
	public int getCount(ITestItem.Status status) {
		Integer counterValue = statusCounters.get(status);
		return (counterValue==null) ? 0 : counterValue;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public boolean wasStopped() {
		return wasStopped;
	}

	public boolean isFinished() {
		return finished;
	}

	public ITestModelAccessor getModelAccessor() {
		return modelManager;
	}
	
	public ILaunch getLaunch() {
		return launch;
	}
	
	public TestsRunnerInfo getTestsRunnerInfo() {
		return testsRunnerInfo;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}

	public String getName() {
		String launchConfName = launch.getLaunchConfiguration().getName();
		String startTimeStr = DateFormat.getDateTimeInstance().format(new Date(startTime));
		return MessageFormat.format("{0} ({1})", launchConfName, startTimeStr);
	}

	public void stop() {
		if (!launch.isTerminated() && launch.canTerminate()) {
			try {
				launch.terminate();
				wasStopped = true;
				statusMessage = "Testing was stopped by user";
			} catch (DebugException e) {
				Activator.log(e);
			}
		}
	}
	
}

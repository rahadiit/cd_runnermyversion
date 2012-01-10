package org.eclipse.cdt.testsrunner.internal.launcher;

import org.eclipse.osgi.util.NLS;

public class LauncherMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.launcher.LauncherMessages"; //$NON-NLS-1$
	public static String BaseTestsLaunchDelegate_invalid_tests_runner;
	public static String BaseTestsLaunchDelegate_tests_runner_load_failed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LauncherMessages.class);
	}

	private LauncherMessages() {
	}
}

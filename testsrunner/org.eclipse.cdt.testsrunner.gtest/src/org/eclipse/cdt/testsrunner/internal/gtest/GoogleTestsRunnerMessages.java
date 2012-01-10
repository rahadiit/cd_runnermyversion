package org.eclipse.cdt.testsrunner.internal.gtest;

import org.eclipse.osgi.util.NLS;

public class GoogleTestsRunnerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.gtest.GoogleTestsRunnerMessages"; //$NON-NLS-1$
	public static String GoogleTestsRunner_error_format;
	public static String GoogleTestsRunner_io_error_prefix;
	public static String OutputHandler_getparam_message;
	public static String OutputHandler_unexpected_case_end;
	public static String OutputHandler_unexpected_output;
	public static String OutputHandler_unexpected_suite_end;
	public static String OutputHandler_unknown_error_prefix;
	public static String OutputHandler_unknown_location_format;
	public static String OutputHandler_unknown_test_status;
	public static String OutputHandler_wrong_groups_count;
	public static String OutputHandler_wrong_suite_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, GoogleTestsRunnerMessages.class);
	}

	private GoogleTestsRunnerMessages() {
	}
}

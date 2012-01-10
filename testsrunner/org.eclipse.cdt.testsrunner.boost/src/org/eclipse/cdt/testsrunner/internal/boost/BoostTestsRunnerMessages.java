package org.eclipse.cdt.testsrunner.internal.boost;

import org.eclipse.osgi.util.NLS;

public class BoostTestsRunnerMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.boost.BoostTestsRunnerMessages"; //$NON-NLS-1$
	public static String BoostTestsRunner_error_format;
	public static String BoostTestsRunner_io_error_prefix;
	public static String BoostTestsRunner_wrong_tests_paths_count;
	public static String BoostTestsRunner_xml_error_prefix;
	public static String BoostXmlLogHandler_exception_suffix;
	public static String BoostXmlLogHandler_wrong_tag_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, BoostTestsRunnerMessages.class);
	}

	private BoostTestsRunnerMessages() {
	}
}

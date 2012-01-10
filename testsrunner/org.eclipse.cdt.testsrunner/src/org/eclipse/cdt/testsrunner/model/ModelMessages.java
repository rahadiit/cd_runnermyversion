package org.eclipse.cdt.testsrunner.model;

import org.eclipse.osgi.util.NLS;

public class ModelMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.model.ModelMessages"; //$NON-NLS-1$
	public static String MessageLevel_error;
	public static String MessageLevel_exception;
	public static String MessageLevel_fatal_error;
	public static String MessageLevel_info;
	public static String MessageLevel_message;
	public static String MessageLevel_warning;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ModelMessages.class);
	}

	private ModelMessages() {
	}
}

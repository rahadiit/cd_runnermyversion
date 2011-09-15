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
package org.eclipse.cdt.testsrunner.internal.boost;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;


/**
 * The activator class controls the plug-in life cycle
 */
public class BoostTestsRunnerPlugin extends Plugin {

	/** The plug-in ID .*/
	public static final String PLUGIN_ID = "org.eclipse.cdt.testsrunner.boost"; //$NON-NLS-1$

	/** Plug-in instance. */
	private static BoostTestsRunnerPlugin plugin;


	public BoostTestsRunnerPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the Boost Tests Runner Plug-in instance.
	 * 
	 * @return the plug-in instance
	 */
	public static BoostTestsRunnerPlugin getDefault() {
		return plugin;
	}

	/** Convenience method which returns the unique identifier of this plugin. */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

}

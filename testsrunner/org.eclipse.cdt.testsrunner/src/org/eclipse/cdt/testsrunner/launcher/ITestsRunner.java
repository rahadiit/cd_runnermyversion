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
package org.eclipse.cdt.testsrunner.launcher;

import java.io.InputStream;

import org.eclipse.cdt.testsrunner.model.IModelManager;


/**
 * TODO: Add descriptions
 * 
 */
public interface ITestsRunner {

	String[] configureLaunchParameters(String[] commandLine);
	
	void run(IModelManager modelBuilder, InputStream inputStream);
	
}

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
package org.eclipse.cdt.testsrunner.model.internal;

import org.eclipse.cdt.testsrunner.model.ITestLocation;

/**
 * TODO: Add descriptions
 * 
 */
public class TestLocation implements ITestLocation {

	private String file;

	private int line;

	
	public TestLocation(String file, int line) {
		this.file = file;
		this.line = line;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}
}

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

import org.eclipse.cdt.testsrunner.model.ITestMessage;

/**
 * TODO: Add descriptions
 * 
 */
public class TestMessage implements ITestMessage {

	private TestLocation location;

	private Level level;

	private String text;

	
	public TestMessage(TestLocation location, Level level, String text) {
		this.location = location;
		this.level = level;
		this.text = text;
	}

	public TestLocation getLocation() {
		return location;
	}

	public Level getLevel() {
		return level;
	}

	public String getText() {
		return text;
	}
}

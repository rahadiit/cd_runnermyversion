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
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;

/**
 * Default DSF Process Factory for inferior processes
 * 
 */
public class InferiorRuntimeProcessFactory implements IProcessFactory {

	public IProcess newProcess(ILaunch launch, Process process, String label, Map attributes) {
		
		return new InferiorRuntimeProcess(launch, process, label, attributes);
	}

}

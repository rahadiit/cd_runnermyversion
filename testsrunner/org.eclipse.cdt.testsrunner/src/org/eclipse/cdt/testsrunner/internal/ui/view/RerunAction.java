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
package org.eclipse.cdt.testsrunner.internal.ui.view;


import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

/**
 * Toggles tests tree hierarchy auto-scroll
 */
public class RerunAction extends Action {

	public RerunAction() {
		super("Rerun");
		setToolTipText("Rerun Test"); // TODO: Add detailed tooltip
		setDisabledImageDescriptor(Activator.getImageDescriptor("dlcl16/rerun.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(Activator.getImageDescriptor("elcl16/rerun.gif")); //$NON-NLS-1$
		setImageDescriptor(Activator.getImageDescriptor("elcl16/rerun.gif")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		ILaunch launch = Activator.getDefault().getTestsRunnersManager().getCurrentLaunch();
		if (launch != null) {
			ILaunchConfiguration launchConf = launch.getLaunchConfiguration();
			if (launchConf != null) {
				DebugUITools.launch(launchConf, launch.getLaunchMode());
				return;
			}
		}
		setEnabled(false);
	}
	
}


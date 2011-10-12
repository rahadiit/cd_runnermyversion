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
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.jface.action.Action;

/**
 * TODO: Add description here
 */
public class StopAction extends Action {

	private TestingSessionsManager testingSessionsManager;

	public StopAction(TestingSessionsManager testingSessionsManager) {
		super("Stop");
		setToolTipText("Stop Test Run");
		setDisabledImageDescriptor(Activator.getImageDescriptor("dlcl16/stop.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(Activator.getImageDescriptor("elcl16/stop.gif")); //$NON-NLS-1$
		setImageDescriptor(Activator.getImageDescriptor("elcl16/stop.gif")); //$NON-NLS-1$
		this.testingSessionsManager = testingSessionsManager;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		ITestingSession activeSession = testingSessionsManager.getActiveSession();
		if (activeSession != null) {
			activeSession.stop();
		}
		setEnabled(false);
	}
	
}


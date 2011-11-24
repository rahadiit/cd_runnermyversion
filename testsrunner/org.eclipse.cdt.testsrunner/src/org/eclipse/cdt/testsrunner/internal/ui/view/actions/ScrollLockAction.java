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
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.UIUpdater;
import org.eclipse.jface.action.Action;

/**
 * Toggles tests tree hierarchy auto-scroll
 */
public class ScrollLockAction extends Action {

	private UIUpdater modelSyncronizer;

	public ScrollLockAction(UIUpdater modelSyncronizer) {
		super("Scroll Lock");
		this.modelSyncronizer = modelSyncronizer;
		setToolTipText("Scroll Lock"); // TODO: Add detailed tooltip
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/scroll_lock.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/scroll_lock.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/scroll_lock.gif")); //$NON-NLS-1$
		setChecked(!this.modelSyncronizer.getAutoScroll());
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		modelSyncronizer.setAutoScroll(!isChecked());
	}
}


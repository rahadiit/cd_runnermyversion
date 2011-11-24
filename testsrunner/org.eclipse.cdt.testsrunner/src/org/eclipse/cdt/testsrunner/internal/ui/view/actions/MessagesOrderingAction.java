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
import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesPanel;
import org.eclipse.jface.action.Action;

/**
 *  TODO: Add description here (and check other actions)!
 */
public class MessagesOrderingAction extends Action {

	private MessagesPanel messagesPanel;


	public MessagesOrderingAction(MessagesPanel messagesPanel) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.messagesPanel = messagesPanel;
		setText("Messages &Ordering");
		setToolTipText("Order Messages By Location, Skip Duplicates");
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/sort.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/sort.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/sort.gif")); //$NON-NLS-1$
		setChecked(messagesPanel.getOrderingMode());
	}

	@Override
	public void run() {
		messagesPanel.setOrderingMode(isChecked());
	}

}

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


import org.eclipse.jface.action.Action;

/**
 *  TODO: Add description here (and check other actions)!
 */
public class ShowFileNameOnlyAction extends Action {

	private MessagesPanel messagesPanel;


	public ShowFileNameOnlyAction(MessagesPanel messagesPanel) {
		super("Show File Names Only", AS_CHECK_BOX); //$NON-NLS-1$
		this.messagesPanel = messagesPanel;
		setToolTipText("Show Only File Names in Message Locations");
		setChecked(messagesPanel.getShowFileNameOnly());
	}

	@Override
	public void run() {
		messagesPanel.setShowFileNameOnly(isChecked());
	}

}

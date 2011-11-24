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


import org.eclipse.cdt.testsrunner.internal.ui.view.MessagesPanel;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 *  TODO :Add description here...
 */
public class MessageLevelFilterAction extends Action {

	private MessagesPanel.LevelFilter levelFilter;
	private MessagesPanel messagePanel;
	
	
	public MessageLevelFilterAction(MessagesPanel messagePanel, MessagesPanel.LevelFilter levelFilter, boolean checked) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.levelFilter = levelFilter;
		this.messagePanel = messagePanel;
		if (levelFilter == MessagesPanel.LevelFilter.Info) {
			setText("&Info");
			setToolTipText("Show information messages");
		} else if (levelFilter == MessagesPanel.LevelFilter.Warning) {
			setText("&Warning");
			setToolTipText("Show warning messages");
		} else if (levelFilter == MessagesPanel.LevelFilter.Error) {
			setText("&Errors");
			setToolTipText("Show error messages");
		}
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(levelFilter.getImageId()));
		setChecked(checked);
		if (checked) {
			messagePanel.addLevelFilter(levelFilter, false);
		}
	}

	@Override
	public void run() {
		if (isChecked()) {
			messagePanel.addLevelFilter(levelFilter, true);
		} else {
			messagePanel.removeLevelFilter(levelFilter);
		}
	}

}

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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ResultsView extends ViewPart {

	enum Orientation {
		Horizontal,
		Vertical,
		Auto,
	}
	
	private ProgressCountPanel progressCountPanel;
	private ResultsPanel resultsPanel;
	private ModelSynchronizer modelSynchronizer;


	@Override
	public void createPartControl(Composite parent) {
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		parent.setLayout(gridLayout);

		progressCountPanel = new ProgressCountPanel(parent, Orientation.Horizontal);
		resultsPanel = new ResultsPanel(parent);
		modelSynchronizer = new ModelSynchronizer(resultsPanel.getTestsHierarchyViewer().getTreeViewer(), progressCountPanel);
		Activator.getDefault().getModelManager().addChangesListener(modelSynchronizer);
	}

	@Override
	public void setFocus() {
		// TODO : Handle focus HERE!
	}

	public void dispose() {
		Activator.getDefault().getModelManager().removeChangesListener(modelSynchronizer);
	}

}

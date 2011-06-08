/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Michels, stephan@apache.org - 104944 [JUnit] Unnecessary code in JUnitProgressBar
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ResultsView extends ViewPart {
	
	TestsHierarchyViewer testsHierarchyViewer;
	


	@Override
	public void createPartControl(Composite parent) {
		testsHierarchyViewer = new TestsHierarchyViewer(parent);
	}
	
// TODO: Remove!
//	public void updateFrom(InputStream inputStream) {
//		modelManager = new ModelManager();
//
//		// TODO: Remove this!
//		StringBuilder sb = new StringBuilder();
//		for (TestsRunnersManager.TestsRunnerInfo tr : Activator.getDefault().getTestsRunnersManager().getTestsRunnersInfo()) {
//			sb.append(" - "+tr.getName()); //$NON-NLS-1$
//			tr.getTestsRunner().run(modelManager, inputStream); 
//		}
//		System.out.print(sb.toString());
//		
//		treeViewer.setInput(modelManager.getRootSuite());
//		treeViewer.refresh();
//		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
//			public void doubleClick(DoubleClickEvent event) {
//				modelManager.enterTestSuite("TestBoostDemo");
//				modelManager.enterTestSuite("s2");
//				modelManager.enterTestCase("!!!newCase!!!");
//				Object tsObj = modelManager.testSuitesStack.peek();
//				Object csObj = modelManager.currentTestCase;
//				modelManager.exitTestSuite();
//				treeViewer.add(tsObj, csObj);
//			}
//		});
//	}

	@Override
	public void setFocus() {
		
	}

}

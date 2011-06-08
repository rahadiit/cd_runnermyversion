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

import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.internal.ModelBuilder;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class ResultsView extends ViewPart {
	
	TreeViewer treeViewer;

	
	public class TestTreeContentProvider implements ITreeContentProvider {

		private final Object[] NO_CHILDREN= new Object[0];

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return ((ITestItem) parentElement).getChildren();
		}

		public Object[] getElements(Object object) {
			return getChildren(object);
		}

		public Object getParent(Object object) {
			return ((ITestItem) object).getParent();
		}

		public boolean hasChildren(Object object) {
			return ((ITestItem) object).hasChildren();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
	
	
	public class TestLabelProvider extends LabelProvider implements ILabelProvider {

		@Override
		public String getText(Object element) {
			return ((ITestItem)element).getName();
		}

//		@Override
//		public Image getImage(Object element) {
//			if (element instanceof TestCaseElement) {
//				TestCaseElement testCaseElement= ((TestCaseElement) element);
//				if (testCaseElement.isIgnored())
//					return fTestRunnerPart.fTestIgnoredIcon;
//
//				Status status=testCaseElement.getStatus();
//				if (status.isNotRun())
//					return fTestRunnerPart.fTestIcon;
//				else if (status.isRunning())
//					return fTestRunnerPart.fTestRunningIcon;
//				else if (status.isError())
//					return fTestRunnerPart.fTestErrorIcon;
//				else if (status.isFailure())
//					return fTestRunnerPart.fTestFailIcon;
//				else if (status.isOK())
//					return fTestRunnerPart.fTestOkIcon;
//				else
//					throw new IllegalStateException(element.toString());
//
//			} else if (element instanceof TestSuiteElement) {
//				Status status= ((TestSuiteElement) element).getStatus();
//				if (status.isNotRun())
//					return fTestRunnerPart.fSuiteIcon;
//				else if (status.isRunning())
//					return fTestRunnerPart.fSuiteRunningIcon;
//				else if (status.isError())
//					return fTestRunnerPart.fSuiteErrorIcon;
//				else if (status.isFailure())
//					return fTestRunnerPart.fSuiteFailIcon;
//				else if (status.isOK())
//					return fTestRunnerPart.fSuiteOkIcon;
//				else
//					throw new IllegalStateException(element.toString());
//
//			} else {
//				throw new IllegalArgumentException(String.valueOf(element));
//			}
//		}

	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		ModelBuilder builder = new ModelBuilder();
		builder.enterTestSuite("Master Suite");
		builder.enterTestSuite("Suite 1");
		builder.enterTestCase("Case 1.1");
		builder.enterTestCase("Case 1.2");
		builder.enterTestCase("Case 1.3");
		builder.exitTestSuite();         
		builder.enterTestSuite("Suite 2");
		builder.enterTestCase("Case 2.1");
		builder.enterTestCase("Case 2.2");
		builder.enterTestCase("Case 2.3");
		builder.exitTestSuite();         
		builder.enterTestSuite("Suite 3");
		builder.enterTestCase("Case 3.1");
		builder.enterTestCase("Case 3.2");
		builder.enterTestCase("Case 3.3");
		builder.exitTestSuite();
		builder.exitTestSuite();
		
		treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.SINGLE);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new TestLabelProvider());
		treeViewer.setInput(builder.getRootSuite());
	}

	@Override
	public void setFocus() {
		
	}


}

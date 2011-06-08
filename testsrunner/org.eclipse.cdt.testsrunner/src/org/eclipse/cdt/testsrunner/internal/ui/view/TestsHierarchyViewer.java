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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class TestsHierarchyViewer {
	
	TreeViewer treeViewer;

	
	class TestTreeContentProvider implements ITreeContentProvider {

		private final Object[] NO_CHILDREN = new Object[0];

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

		public void dispose() {
		}

	}

	class TestLabelProvider extends LabelProvider implements IStyledLabelProvider {

		private Map<ITestItem.Status, Image> testCaseImages = new HashMap<ITestItem.Status, Image>();
		{
			testCaseImages.put(ITestItem.Status.Skipped, Activator.createAutoImage("obj16/test_skipped.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Passed, Activator.createAutoImage("obj16/test_passed.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Failed, Activator.createAutoImage("obj16/test_failed.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Aborted, Activator.createAutoImage("obj16/test_aborted.gif")); //$NON-NLS-1$
		}
		private Image testCaseRunImage = Activator.createAutoImage("obj16/test_run.gif"); //$NON-NLS-1$


		private Map<ITestItem.Status, Image> testSuiteImages = new HashMap<ITestItem.Status, Image>();
		{
			testSuiteImages.put(ITestItem.Status.Skipped, Activator.createAutoImage("obj16/tsuite_passed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Passed, Activator.createAutoImage("obj16/tsuite_passed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Failed, Activator.createAutoImage("obj16/tsuite_failed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Aborted, Activator.createAutoImage("obj16/tsuite_aborted.gif")); //$NON-NLS-1$
		}
		private Image testSuiteRunImage = Activator.createAutoImage("obj16/tsuite_run.gif"); //$NON-NLS-1$


	    public Image getImage(Object element) {
	    	Map<ITestItem.Status, Image> imagesMap = null;
	    	Image runImage = null;
	    	if (element instanceof ITestCase) {
	    		imagesMap = testCaseImages;
	    		runImage = testCaseRunImage;
	    		
	    	} else if (element instanceof ITestSuite) {
	    		imagesMap = testSuiteImages;
	    		runImage = testSuiteRunImage;
	    	}
	    	if (imagesMap != null) {
	    		ITestItem testItem = (ITestItem)element;
				if (Activator.getDefault().getModelManager().isCurrentlyRunning(testItem)) {
					return runImage;
				}
				return imagesMap.get(testItem.getStatus());
	    	}
	    	
	    	return null;
	    }

		public String getText(Object element) {
			StringBuilder sb = new StringBuilder();
			sb.append(((ITestItem) element).getName());
			sb.append(getTestingTimeString(element));
			return sb.toString();
		}

		public StyledString getStyledText(Object element) {
			ITestItem testItem = (ITestItem)element;
			StringBuilder labelBuf = new StringBuilder();
			labelBuf.append(testItem.getName());
			StyledString name = new StyledString(labelBuf.toString());
			String time = getTestingTimeString(element);
			labelBuf.append(time);
			name = StyledCellLabelProvider.styleDecoratedString(labelBuf.toString(), StyledString.COUNTER_STYLER, name);
			
			return name;
		}
		
		private String getTestingTimeString(Object element) {
			// TODO: Add a message template and internalize it!
			return (element instanceof ITestItem) ? " ("+Double.toString(((ITestItem)element).getTestingTime()/1000.0)+" s)" : "";
		}
	}

	public TestsHierarchyViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.MULTI);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new ColoringLabelProvider(new TestLabelProvider()));
		treeViewer.setInput(Activator.getDefault().getModelManager().getRootSuite());
	}
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
}

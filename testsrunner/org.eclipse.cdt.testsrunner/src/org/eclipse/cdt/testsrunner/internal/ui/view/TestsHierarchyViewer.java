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
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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

	class TestLabelProvider extends LabelProvider implements
			ILabelProvider {

		@Override
		public String getText(Object element) {
			// TODO: Make code clean up
			StringBuilder sb = new StringBuilder();
			if (Activator.getDefault().getModelManager()
					.isCurrentlyRunning((ITestItem) element)) {
				sb.append(">> ");
			}
			sb.append(((ITestItem) element).getName());
			if (element instanceof ITestCase) {
				sb.append(": ");
				sb.append(((ITestCase) element).getStatus().toString());
			}
			return sb.toString();
		}
	}

	public TestsHierarchyViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.MULTI);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new TestLabelProvider());
		treeViewer.setInput(Activator.getDefault().getModelManager().getRootSuite());
	}
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
}

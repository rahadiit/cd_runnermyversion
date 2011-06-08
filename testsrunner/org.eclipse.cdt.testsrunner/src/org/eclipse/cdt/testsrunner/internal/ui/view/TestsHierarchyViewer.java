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

import org.eclipse.cdt.testsrunner.internal.Activator;
import org.eclipse.cdt.testsrunner.model.IModelManagerListener;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class TestsHierarchyViewer {
	
	TreeViewer treeViewer;
	ModelManagerListener modelListener;

	
	public class TestTreeContentProvider implements ITreeContentProvider {

		private final Object[] NO_CHILDREN = new Object[0];

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

	public class TestLabelProvider extends LabelProvider implements
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

	public class ModelManagerListener implements IModelManagerListener {

		TreeViewer treeViewer;

		ModelManagerListener(TreeViewer treeViewer) {
			this.treeViewer = treeViewer;
		}

		public void enterTestSuite(ITestSuite testSuite) {
			// TODO: Update only necessary properties!
			// treeViewer.update(testSuite, null);
			Display.getDefault().syncExec(
					new ExpandRunnable(treeViewer, testSuite));
		}

		public void exitTestSuite(ITestSuite testSuite) {
			// TODO: Update only necessary properties!
			// treeViewer.update(testSuite, null);
			Display.getDefault().syncExec(new CollapseAllRunnable(treeViewer));
		}

		public void enterTestCase(ITestCase testCase) {
			// TODO: Update only necessary properties!
			// treeViewer.update(testCase, null);
			Display.getDefault().syncExec(
					new ExpandRunnable(treeViewer, testCase));
		}

		public void exitTestCase(ITestCase testCase) {
			// TODO: Update only necessary properties!
			// treeViewer.update(testCase, null);
			Display.getDefault().syncExec(
					new UpdateRunnable(treeViewer, testCase));
		}

		public void addTestSuite(ITestSuite parent, ITestSuite child) {
			Display.getDefault().syncExec(
					new AddObjectRunnable(treeViewer, parent, child));
		}

		public void addTestCase(ITestSuite parent, ITestCase child) {
			Display.getDefault().syncExec(
					new AddObjectRunnable(treeViewer, parent, child));
		}

		public void refreshModel() {
			Display.getDefault().syncExec(new RefreshRunnable(treeViewer));
		}

		class AddObjectRunnable implements Runnable {
			TreeViewer treeViewer;
			Object parent;
			Object child;

			AddObjectRunnable(TreeViewer treeViewer, Object parent, Object child) {
				this.treeViewer = treeViewer;
				this.parent = parent;
				this.child = child;
			}

			public void run() {
				treeViewer.add(parent, child);
			}
		}

		class ExpandRunnable implements Runnable {
			TreeViewer treeViewer;
			Object object;

			ExpandRunnable(TreeViewer treeViewer, Object object) {
				this.treeViewer = treeViewer;
				this.object = object;
			}

			public void run() {
				// treeViewer.expandToLevel(object, 0);
				treeViewer.reveal(object);
			}
		}

		class CollapseAllRunnable implements Runnable {
			TreeViewer treeViewer;

			CollapseAllRunnable(TreeViewer treeViewer) {
				this.treeViewer = treeViewer;
			}

			public void run() {
				treeViewer.collapseAll();
			}
		}

		class UpdateRunnable implements Runnable {
			TreeViewer treeViewer;
			Object object;

			UpdateRunnable(TreeViewer treeViewer, Object object) {
				this.treeViewer = treeViewer;
				this.object = object;
			}

			public void run() {
				treeViewer.update(object, null);
			}
		}

		class RefreshRunnable implements Runnable {
			TreeViewer treeViewer;

			RefreshRunnable(TreeViewer treeViewer) {
				this.treeViewer = treeViewer;
			}

			public void run() {
				treeViewer.refresh();
			}
		}

	}
	
	public TestsHierarchyViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.SINGLE);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new TestLabelProvider());
		treeViewer.setInput(Activator.getDefault().getModelManager().getRootSuite());
		modelListener = new ModelManagerListener(treeViewer);
		Activator.getDefault().getModelManager().addChangesListener(modelListener);
	}
	
	public void dispose() {
		Activator.getDefault().getModelManager().removeChangesListener(modelListener);
	}

}

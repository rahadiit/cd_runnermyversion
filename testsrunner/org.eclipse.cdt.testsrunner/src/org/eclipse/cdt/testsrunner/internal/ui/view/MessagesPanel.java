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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * TODO: Fix description
 */
public class MessagesPanel {

	private TableViewer tableViewer;

	
	class MessagesCollector implements IModelVisitor {
		
		Set<ITestMessage> testMessages;
		
		MessagesCollector(Set<ITestMessage> testMessages) {
			this.testMessages = testMessages;
		}
		
		public void visit(ITestMessage testMessage) {
			testMessages.add(testMessage);
		}
		
		public void visit(ITestCase testCase) {}
		public void visit(ITestSuite testSuite) {}
	}

	class MessagesContentProvider implements IStructuredContentProvider {
		
		ITestMessage[] testMessages;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			collectMessages((ITestItem[]) newInput);
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return testMessages;
		}

		private void collectMessages(ITestItem[] testItems) {
			Set<ITestMessage> testMessagesSet = new HashSet<ITestMessage>();
			for (ITestItem testItem : testItems) {
				testItem.visit(new MessagesCollector(testMessagesSet));
			}
			testMessages = testMessagesSet.toArray(new ITestMessage[testMessagesSet.size()]);
		}
	}

	class MessagesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return ((ITestMessage)obj).getText();
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			// TODO: Refactor this later!
			String str = ISharedImages.IMG_OBJ_ELEMENT;
			switch (((ITestMessage)obj).getLevel()) {
				case Info:
				case Message:
					str = ISharedImages.IMG_OBJS_INFO_TSK;
					break;
				case Warning:
					str = ISharedImages.IMG_OBJS_WARN_TSK;
					break;
				case Error:
				case FatalError:
				case Exception:
					str = ISharedImages.IMG_OBJS_ERROR_TSK;
					break;
			}
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(str);
		}
	}
	
	
	public MessagesPanel(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.setLabelProvider(new MessagesLabelProvider());
		tableViewer.setContentProvider(new MessagesContentProvider());
	}

	public Control getControl() {
		return tableViewer.getControl();
	}
	
	public void showItemsMessages(ITestItem[] testItems) {
		tableViewer.setInput(testItems);
	}
	
}

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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestLocation;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * TODO: Fix description
 * TODO: Rename MessagesPanel => MessagesViewer (for consistency)
 */
public class MessagesPanel {

	class MessagesCollector implements IModelVisitor {
		
		Set<ITestMessage> testMessages;
		boolean collect = true;
		
		MessagesCollector(Set<ITestMessage> testMessages) {
			this.testMessages = testMessages;
		}
		
		public void visit(ITestMessage testMessage) {
			if (collect) {
				testMessages.add(testMessage);
			}
		}
		
		public void visit(ITestCase testCase) {
			collect = !showFailedOnly.get() || testCase.getStatus().isError();
		}
		
		public void visit(ITestSuite testSuite) {}
	}

	class MessagesContentProvider implements IStructuredContentProvider {
		
		ITestMessage[] testMessages;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput != null) {
				collectMessages((ITestItem[]) newInput);
			} else {
				testMessages = new ITestMessage[0];
			}
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			return testMessages;
		}
		
		private void collectMessages(ITestItem[] testItems) {
			Set<ITestMessage> testMessagesSet = new TreeSet<ITestMessage>(new Comparator<ITestMessage>() {

				public int compare(ITestMessage message1, ITestMessage message2) {
					// Compare messages by location
					ITestLocation location1 = message1.getLocation();
					ITestLocation location2 = message2.getLocation();
					
					if (location1 != null && location2 != null) {
						// Compare by file name
						String file1 = location1.getFile();
						String file2 = location2.getFile();
						int fileResult = file1.compareTo(file2);
						if (fileResult != 0) {
							return fileResult;
						} else {
							// Compare by line number
							int line1 = location1.getLine();
							int line2 = location2.getLine();
							if (line1 < line2) {
								return -1;
								
							} else if (line1 > line2) {
								return 1;
							}
						}
						
					} else if (location1 == null && location2 != null) {
						return -1;
						
					} else if (location1 != null && location2 == null) {
						return 1;
					}
					
					// Compare by message text
					String text1 = message1.getText();
					String text2 = message2.getText();
					return text1.compareTo(text2);
				}
			});
			for (ITestItem testItem : testItems) {
				testItem.visit(new MessagesCollector(testMessagesSet));
			}
			testMessages = testMessagesSet.toArray(new ITestMessage[testMessagesSet.size()]);
		}
	}

	class MessagesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			StringBuilder sb = new StringBuilder();
			ITestMessage message = (ITestMessage)obj;
			ITestLocation location = message.getLocation();
			if (location != null) {
				sb.append(location.getFile());
				sb.append("(");
				sb.append(location.getLine());
				sb.append("): ");
			}
			switch (message.getLevel()) {
				case Info:
					sb.append("Info");
					break;
				case Message:
					sb.append("Message");
					break;
				case Warning:
					sb.append("Warning");
					break;
				case Error:
					sb.append("Error");
					break;
				case FatalError:
					sb.append("Fatal error");
					break;
				case Exception:			
					sb.append("Exception");
					break;
			}
			sb.append(": ");
			sb.append(message.getText());
			return sb.toString();
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
	
	
	private TableViewer tableViewer;
	private OpenInEditorAction openInEditorAction;
	private ResultsPanel.ShowFailedOnlyKeeper showFailedOnly;
	

	public MessagesPanel(Composite parent, ResultsPanel.ShowFailedOnlyKeeper showFailedOnly) {
		this.showFailedOnly = showFailedOnly;
		tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.setLabelProvider(new MessagesLabelProvider());
		tableViewer.setContentProvider(new MessagesContentProvider());
		
		openInEditorAction = new OpenInEditorAction(tableViewer);
		tableViewer.addOpenListener(new IOpenListener() {
			
			public void open(OpenEvent event) {
				openInEditorAction.run();
			}
		});
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	public void showItemsMessages(ITestItem[] testItems) {
		tableViewer.setInput(testItems);
	}

	public void applyFilterChanges() {
		// NOTE: Set input again makes content provider to recollect messages (with filter applied)
		tableViewer.setInput(tableViewer.getInput());
		tableViewer.refresh();
	}
	
}

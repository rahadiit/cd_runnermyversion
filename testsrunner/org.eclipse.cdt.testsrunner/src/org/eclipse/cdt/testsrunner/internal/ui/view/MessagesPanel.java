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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestLocation;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;


/**
 * TODO: Fix description
 * TODO: Rename MessagesPanel => MessagesViewer (for consistency)
 */
public class MessagesPanel {

	enum LevelFilter {
		Info(ISharedImages.IMG_OBJS_INFO_TSK, ITestMessage.Level.Info, ITestMessage.Level.Message),
		Warning(ISharedImages.IMG_OBJS_WARN_TSK, ITestMessage.Level.Warning),
		Error(ISharedImages.IMG_OBJS_ERROR_TSK, ITestMessage.Level.Error, ITestMessage.Level.FatalError, ITestMessage.Level.Exception);

		private String imageId;
		private ITestMessage.Level [] includedLevels;
		
		LevelFilter(String imageId, ITestMessage.Level... includedLevels) {
			this.imageId = imageId;
			this.includedLevels = includedLevels;
		}
		
		public String getImageId() {
			return imageId;
		}
		
		public ITestMessage.Level [] getLevels() {
			return includedLevels;
		}
		
		public boolean isIncluded(ITestMessage.Level searchLevel) {
			for (ITestMessage.Level currLevel : includedLevels) {
				if (currLevel.equals(searchLevel)) {
					return true;
				}
			}
			return false;
		}
	}

	class MessagesContentProvider implements IStructuredContentProvider {
		
		class MessagesCollector implements IModelVisitor {
			
			Collection<ITestMessage> testMessages;
			boolean collect = true;
			
			MessagesCollector(Collection<ITestMessage> testMessages) {
				this.testMessages = testMessages;
			}
			
			public void visit(ITestMessage testMessage) {
				if (collect) {
					testMessages.add(testMessage);
				}
			}
			
			public void visit(ITestCase testCase) {
				collect = !showFailedOnly || testCase.getStatus().isError();
			}
			
			public void visit(ITestSuite testSuite) {}

			public void leave(ITestSuite testSuite) {}

			public void leave(ITestCase testCase) {}

			public void leave(ITestMessage testMessage) {}
		}

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
		
		private Collection<ITestMessage> createMessagesSet() {
			return new TreeSet<ITestMessage>(new Comparator<ITestMessage>() {

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
		}
		
		private Collection<ITestMessage> createMessagesList() {
			return new ArrayList<ITestMessage>();
		}

		private Collection<ITestMessage> createMessagesCollection() {
			return orderingMode ? createMessagesSet() : createMessagesList();
		}
		
		private void collectMessages(ITestItem[] testItems) {
			Collection<ITestMessage> testMessagesCollection = createMessagesCollection();
			for (ITestItem testItem : testItems) {
				testItem.visit(new MessagesCollector(testMessagesCollection));
			}
			testMessages = testMessagesCollection.toArray(new ITestMessage[testMessagesCollection.size()]);
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
			Level level = ((ITestMessage)obj).getLevel();
			String imageId = ISharedImages.IMG_OBJ_ELEMENT;
			for (LevelFilter levelFilter : LevelFilter.values()) {
				if (levelFilter.isIncluded(level)) {
					imageId = levelFilter.getImageId();
					break;
				}
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageId);
		}
	}
	
	class MessageLevelFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return acceptedMessageLevels.contains(((ITestMessage)element).getLevel());
		}
	}


	private TableViewer tableViewer;
	private OpenInEditorAction openInEditorAction;
	private IViewSite viewSite;
	private Action copyAction;
	private boolean showFailedOnly = false;
	private Set<ITestMessage.Level> acceptedMessageLevels = new HashSet<ITestMessage.Level>();
	private boolean orderingMode = false;


	public MessagesPanel(Composite parent,
			TestingSessionsManager sessionsManager, IWorkbench workbench,
			IViewSite viewSite, Clipboard clipboard) {
		this.viewSite = viewSite;
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.setLabelProvider(new MessagesLabelProvider());
		tableViewer.setContentProvider(new MessagesContentProvider());
		tableViewer.addFilter(new MessageLevelFilter());
		initContextMenu(viewSite, sessionsManager, workbench, clipboard);
		tableViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				openInEditorAction.run();
			}
		});
	}

	private void initContextMenu(IViewSite viewSite, TestingSessionsManager sessionsManager, IWorkbench workbench,
			Clipboard clipboard) {
		openInEditorAction = new OpenInEditorAction(tableViewer, sessionsManager, workbench);
		copyAction = new CopySelectedMessagesAction(tableViewer, clipboard);

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		viewSite.registerContextMenu(menuMgr, tableViewer);
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		tableViewer.getTable().setMenu(menu);

		menuMgr.add(openInEditorAction);
		menuMgr.add(copyAction);
		configureCopy();
	}
	
	private void configureCopy() {
		getTableViewer().getTable().addFocusListener(new FocusListener() {
        	IAction viewCopyHandler;

        	public void focusLost(FocusEvent e) {
        		if (viewCopyHandler != null) {
        			switchTo(viewCopyHandler);
        		}
			}

			public void focusGained(FocusEvent e) {
				switchTo(copyAction);
			}
			
			private void switchTo(IAction copyAction) {
				IActionBars actionBars = viewSite.getActionBars();
				viewCopyHandler = actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
				actionBars.updateActionBars();
			}
		});
	}
	
	private void handleMenuAboutToShow(IMenuManager manager) {
		ISelection selection = tableViewer.getSelection();
		openInEditorAction.setEnabled(!selection.isEmpty());
		copyAction.setEnabled(!selection.isEmpty());
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	public void showItemsMessages(ITestItem[] testItems) {
		tableViewer.setInput(testItems);
	}
	
	private void forceRecollectMessages() {
		// NOTE: Set input again makes content provider to recollect messages (with filter applied)
		tableViewer.setInput(tableViewer.getInput());
	}

	public boolean getShowFailedOnly() {
		return showFailedOnly;
	}
	
	public void setShowFailedOnly(boolean showFailedOnly) {
		if (this.showFailedOnly != showFailedOnly) {
			this.showFailedOnly = showFailedOnly;
			forceRecollectMessages();
		}
	}

	public boolean getOrderingMode() {
		return showFailedOnly;
	}
	
	public void setOrderingMode(boolean orderingMode) {
		if (this.orderingMode != orderingMode) {
			this.orderingMode = orderingMode;
			forceRecollectMessages();
		}
	}

	public void addLevelFilter(LevelFilter levelFilter, boolean refresh) {
		for (ITestMessage.Level level : levelFilter.getLevels()) {
			acceptedMessageLevels.add(level);
		}
		if (refresh) {
			tableViewer.refresh();
		}
	}

	public void removeLevelFilter(LevelFilter levelFilter) {
		for (ITestMessage.Level level : levelFilter.getLevels()) {
			acceptedMessageLevels.remove(level);
		}
		tableViewer.refresh();
	}
	
}

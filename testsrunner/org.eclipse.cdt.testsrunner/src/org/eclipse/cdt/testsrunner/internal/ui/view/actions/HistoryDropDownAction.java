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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.model.TestingSessionsManager;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * TODO: Add description here!
 */
public class HistoryDropDownAction extends Action {
	
	private class HistoryListDialog extends StatusDialog {
		
		private static final int MAX_HISTORY_SIZE = 100;
		private ListDialogField<ITestingSession> historyList;
		private StringDialogField maxEntriesField;
		private int historySize;

		private ITestingSession resultSession;


		private final class TestRunLabelProvider extends LabelProvider {

			@Override
			public String getText(Object element) {
				return ((ITestingSession)element).getName();
			}

			@Override
			public Image getImage(Object element) {
				// TODO: Add icons to test sessions in the dialog!
				return null;
			}

		}

		
		private HistoryListDialog(Shell shell) {
			super(shell);
			setHelpAvailable(false);
			setTitle("Test Runs");

			createHistoryList();
			createMaxEntriesField();
		}

		/*
		 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
		 * @since 3.4
		 */
		@Override
		protected boolean isResizable() {
			return true;
		}

		private void createHistoryList() {
			IListAdapter<ITestingSession> adapter = new IListAdapter<ITestingSession>() {
				public void customButtonPressed(ListDialogField<ITestingSession> field, int index) {
					doCustomButtonPressed(index);
				}
				public void selectionChanged(ListDialogField<ITestingSession> field) {
					doSelectionChanged();
				}

				public void doubleClicked(ListDialogField<ITestingSession> field) {
					doDoubleClicked();
				}
			};
			String[] buttonLabels = new String[] { "&Remove", "Remove &All" };
			LabelProvider labelProvider = new TestRunLabelProvider();
			historyList = new ListDialogField<ITestingSession>(adapter, buttonLabels, labelProvider);
			historyList.setLabelText("Select a test run:");

			historyList.setElements(testingSessionsManager.getSessions());
			Object currentEntry = testingSessionsManager.getActiveSession();
			ISelection sel = (currentEntry != null) ? new StructuredSelection(currentEntry) : new StructuredSelection();
			historyList.selectElements(sel);
		}

		private void createMaxEntriesField() {
			maxEntriesField = new StringDialogField();
			maxEntriesField.setLabelText("&Maximum count of remembered test runs:");
			maxEntriesField.setDialogFieldListener(new IDialogFieldListener() {
				public void dialogFieldChanged(DialogField field) {
					String maxString = maxEntriesField.getText();
					boolean valid;
					try {
						historySize = Integer.parseInt(maxString);
						valid = historySize > 0 && historySize < MAX_HISTORY_SIZE;
					} catch (NumberFormatException e) {
						valid = false;
					}
					IStatus status = valid ? StatusInfo.OK_STATUS : new StatusInfo(IStatus.ERROR, 
							MessageFormat.format("Please enter a positive integer smaller than {0}.", 
									Integer.toString(MAX_HISTORY_SIZE)
							)
						);
					updateStatus(status);
				}
			});
			maxEntriesField.setText(Integer.toString(testingSessionsManager.getHistorySize()));
		}

		/*
		 * @see Dialog#createDialogArea(Composite)
		 */
		@Override
		protected Control createDialogArea(Composite parent) {
			initializeDialogUnits(parent);

			Composite composite = (Composite) super.createDialogArea(parent);

			Composite inner= new Composite(composite, SWT.NONE);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			inner.setFont(composite.getFont());

			LayoutUtil.doDefaultLayout(inner, new DialogField[] { historyList, new org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator() }, true);
			LayoutUtil.setHeightHint(historyList.getListControl(null), convertHeightInCharsToPixels(12));
			LayoutUtil.setHorizontalGrabbing(historyList.getListControl(null));

			Composite additionalControls = new Composite(inner, SWT.NONE);
			additionalControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			LayoutUtil.doDefaultLayout(additionalControls, new DialogField[] { maxEntriesField }, false);
			LayoutUtil.setHorizontalGrabbing(maxEntriesField.getTextControl(null));

			applyDialogFont(composite);
			return composite;
		}

		private void doCustomButtonPressed(int index) {
			switch (index) {
				case 0: // remove
					historyList.removeElements(historyList.getSelectedElements());
					historyList.selectFirstElement();
					break;

				case 1: // remove all
					historyList.removeAllElements();
					break;
				default:
					break;
			}
		}

		private void doDoubleClicked() {
			okPressed();
		}

		private void doSelectionChanged() {
			List<ITestingSession> selected = historyList.getSelectedElements();
			if (selected.size() >= 1) {
				resultSession = selected.get(0);
			} else {
				resultSession = null;
			}
			historyList.enableButton(0, selected.size() != 0);
		}

		public ITestingSession getResultActiveSession() {
			return resultSession;
		}

		public List<ITestingSession> getResultSessions() {
			return historyList.getElements();
		}

		public int getResultHistorySize() {
			return historySize;
		}
	}

	
	private class HistoryAction extends Action {
		
		private final ITestingSession testingSession;

		public HistoryAction(int testingSessionIndex, ITestingSession testingSession) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			this.testingSession = testingSession;

			// Generate action text
			String label = testingSession.getName();
			if (testingSessionIndex < 10) {
				// Add the numerical accelerator
				label = new StringBuffer().append('&').append(testingSessionIndex).append(' ').append(label).toString();
			}
			setText(label);
		}

		@Override
		public void run() {
			if (isChecked()) {
				testingSessionsManager.setActiveSession(testingSession);
			}
		}
	}
	
	
	private class HistoryListAction extends Action {

		public HistoryListAction() {
			super("History...", AS_RADIO_BUTTON);
		}

		@Override
		public void run() {
			if (isChecked()) {
				runHistoryDialog();
			}
		}
	}
	
	
	private class ClearAction extends Action {
		public ClearAction() {
			setText("&Clear Terminated");

			boolean enabled = false;
			for (ITestingSession testingSession : testingSessionsManager.getSessions()) {
				if (testingSession.isFinished()) {
					enabled = true;
					break;
				}
			}
			setEnabled(enabled);
		}

		@Override
		public void run() {
			List<ITestingSession> remainingSessions = new ArrayList<ITestingSession>();
			for (ITestingSession testingSession : testingSessionsManager.getSessions()) {
				if (!testingSession.isFinished()) {
					remainingSessions.add(testingSession);
				}
			}

			ITestingSession newActiveSession = remainingSessions.isEmpty() ? null : remainingSessions.get(0);
			testingSessionsManager.setActiveSession(newActiveSession);
			testingSessionsManager.setSessions(remainingSessions);
		}
	}

	
	private class HistoryMenuCreator implements IMenuCreator {

		public Menu getMenu(Menu parent) {
			return null;
		}

		public Menu getMenu(Control parent) {
			if (menu != null) {
				menu.dispose();
			}
			final MenuManager manager = new MenuManager();
			manager.setRemoveAllWhenShown(true);
			manager.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager2) {
					boolean checkOthers = addHistoryItems(manager2);

					Action others = new HistoryListAction();
					others.setChecked(checkOthers);
					manager2.add(others);
					manager2.add(new Separator());
					manager2.add(new ClearAction());
				}

				private boolean addHistoryItems(IMenuManager manager) {
					boolean checkOthers = true;
					
					int sessionsCount = testingSessionsManager.getSessionsCount();
					if (sessionsCount == 0) {
						return false;
					}
					int menuItemsCount = Math.min(sessionsCount, RESULTS_IN_DROP_DOWN);
					
					ITestingSession activeSession = testingSessionsManager.getActiveSession();
					int testingSessionIndex = 0;
					for (ITestingSession testingSession : testingSessionsManager.getSessions()) {
						if (testingSessionIndex >= menuItemsCount) {
							break;
						}
						HistoryAction action = new HistoryAction(testingSessionIndex, testingSession);
						boolean check = (testingSession == activeSession);
						action.setChecked(check);
						if (check) {
							checkOthers = false;
						}
						manager.add(action);
						testingSessionIndex++;
					}
					
					return checkOthers;
				}
			});

			menu = manager.createContextMenu(parent);
			return menu;
		}

		public void dispose() {
			if (menu != null) {
				menu.dispose();
				menu = null;
			}
		}
	}
	
	
	public static final int RESULTS_IN_DROP_DOWN = 10;

	private TestingSessionsManager testingSessionsManager;
	private Shell shell;
	private Menu menu;

	
	public HistoryDropDownAction(TestingSessionsManager testingSessionsManager, Shell shell) {
		super("History");
		setToolTipText("Test Run History...");
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/history_list.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/history_list.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/history_list.gif")); //$NON-NLS-1$
		this.testingSessionsManager = testingSessionsManager;
		setMenuCreator(new HistoryMenuCreator());
	}

	private void runHistoryDialog() {
		HistoryListDialog dialog = new HistoryListDialog(shell);
		if (dialog.open() == Window.OK) {
			testingSessionsManager.setHistorySize(dialog.getResultHistorySize());
			testingSessionsManager.setActiveSession(dialog.getResultActiveSession());
			testingSessionsManager.setSessions(dialog.getResultSessions());
		}
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		runHistoryDialog();
	}
	
}


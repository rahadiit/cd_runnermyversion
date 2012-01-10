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
package org.eclipse.cdt.testsrunner.internal.ui.launcher;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnerInfo;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A launch configuration tab that displays and edits different testing options
 * (e.g. Tests Runner Plug-in).
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CTestingTab extends CLaunchConfigurationTab {
	
	/**
	 * Tab identifier used for ordering of tabs added using the 
	 * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
	 * extension point.
	 *   
	 * @since 6.0
	 */
	private static final String TAB_ID = "org.eclipse.cdt.testsrunner.testingTab"; //$NON-NLS-1$

	private static final String TESTING_PROCESS_FACTORY_ID = "org.eclipse.cdt.testsrunner.TestingProcessFactory"; //$NON-NLS-1$

	/** Shows the list of available Tests Runner Plug-ins. */
	private Combo testsRunnerCombo;
	
	/** Shows the description for the currently selected Tests Runner Plug-in. */
	private Label testsRunnerDescriptionLabel;

	public void createControl(Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
		GridLayout pageCompositeLayout = new GridLayout(2, false);
		pageCompositeLayout.horizontalSpacing = 40;
		pageComposite.setLayout(pageCompositeLayout);

		// Create a tests runner selector
		new Label(pageComposite, SWT.NONE).setText(UILauncherMessages.CTestingTab_tests_runner_label);
		testsRunnerCombo = new Combo(pageComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		testsRunnerCombo.add("<not set>"); //$NON-NLS-1$
		testsRunnerCombo.setData("0", null); //$NON-NLS-1$
		
		// Add all the tests runners
    	for (TestsRunnerInfo testsRunnerInfo : TestsRunnerPlugin.getDefault().getTestsRunnersManager().getTestsRunnersInfo()) {
    		testsRunnerCombo.setData(Integer.toString(testsRunnerCombo.getItemCount()), testsRunnerInfo);
    		testsRunnerCombo.add(testsRunnerInfo.getName());
    	}
		
		testsRunnerCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				testsRunnerDescriptionLabel.setText(getCurrentTestsRunnerDescription());
				updateLaunchConfigurationDialog();
			}
		});
		
		// Create a tests runner description label 
		testsRunnerDescriptionLabel = new Label(pageComposite, SWT.WRAP);
		GridData testsRunnerLabelGD = new GridData(GridData.FILL_BOTH);
		testsRunnerLabelGD.horizontalSpan = 2;
		testsRunnerLabelGD.horizontalAlignment = GridData.FILL;
		testsRunnerDescriptionLabel.setLayoutData(testsRunnerLabelGD);
		
		GridData pageCompositeGD = new GridData(GridData.FILL_BOTH);
		pageCompositeGD.horizontalAlignment = GridData.FILL;
		pageCompositeGD.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(pageCompositeGD);
		setControl(pageComposite);
	}
	
	/**
	 * Returns the information for the currently selected Tests Runner Plug-in.
	 * 
	 * @return Tests Runner Plug-in information
	 */
	private ITestsRunnerInfo getCurrentTestsRunnerInfo() {
		return getTestsRunnerInfo(testsRunnerCombo.getSelectionIndex());
	}

	/**
	 * Returns the information for the Tests Runner Plug-in specified by index.
	 * 
	 * @param comboIndex index in combo widget
	 * @return Tests Runner Plug-in information
	 */
	private ITestsRunnerInfo getTestsRunnerInfo(int comboIndex) {
		return (ITestsRunnerInfo)testsRunnerCombo.getData(Integer.toString(comboIndex));
	}

	/**
	 * Returns the description for the currently selected Tests Runner Plug-in.
	 * 
	 * @return the description
	 */
	private String getCurrentTestsRunnerDescription() {
		ITestsRunnerInfo testsRunnerInfo = getCurrentTestsRunnerInfo();
		if (testsRunnerInfo != null) {
			return testsRunnerInfo.getDescription();
		} else {
			return UILauncherMessages.CTestingTab_no_tests_runner_label;
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		return getCurrentTestsRunnerInfo() != null;
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String) null);
		config.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, TESTING_PROCESS_FACTORY_ID);
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String testsRunnerId = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String) null);
			int comboIndex = 0;
			for (int i = 1; i < testsRunnerCombo.getItemCount(); i++) {
				ITestsRunnerInfo testsRunnerInfo = getTestsRunnerInfo(i);
				if (testsRunnerInfo.getId().equals(testsRunnerId)) {
					comboIndex = i;
					break;
				}
			}
			testsRunnerCombo.select(comboIndex);
			
		} catch (CoreException e) {
			TestsRunnerPlugin.log(e);
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		ITestsRunnerInfo testsRunnerInfo = getCurrentTestsRunnerInfo();
		String testsRunnerId = testsRunnerInfo != null ? testsRunnerInfo.getId() : null;
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_TESTS_RUNNER, testsRunnerId);
		configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, TESTING_PROCESS_FACTORY_ID);
	}

	@Override
	public String getId() {
		return TAB_ID;
	}

	public String getName() {
		return UILauncherMessages.CTestingTab_tab_name; 
	}

	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			if (getCurrentTestsRunnerInfo()==null) {
				return UILauncherMessages.CTestingTab_no_tests_runner_error;
			}
		}
		return m;
	}

	public Image getImage() {
		return TestsRunnerPlugin.createAutoImage("obj16/test_notrun.gif"); //$NON-NLS-1$
	}

}

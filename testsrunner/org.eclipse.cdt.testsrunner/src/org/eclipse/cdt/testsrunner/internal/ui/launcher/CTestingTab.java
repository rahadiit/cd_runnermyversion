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

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnersManager.TestsRunnerInfo;
import org.eclipse.core.runtime.CoreException;
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
 * A launch configuration tab that displays and edits program arguments,
 * and working directory launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
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
	private static final String TAB_ID = "org.eclipse.cdt.cdi.launch.testingTab"; //$NON-NLS-1$

	// Program arguments UI widgets
	protected Label testsRunnerDescriptionLabel;
	protected Combo testsRunnerCombo;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
		GridLayout pageCompositeLayout = new GridLayout(2, false);
		pageCompositeLayout.horizontalSpacing = 40;
		pageComposite.setLayout(pageCompositeLayout);

		// Create a tests runner selector
		new Label(pageComposite, SWT.NONE).setText("Tests Runner");
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
	
	private TestsRunnerInfo getCurrentTestsRunnerInfo() {
		return getTestsRunnerInfo(testsRunnerCombo.getSelectionIndex());
	}

	private TestsRunnerInfo getTestsRunnerInfo(int comboIndex) {
		return (TestsRunnerInfo)testsRunnerCombo.getData(Integer.toString(comboIndex));
	}

	private String getCurrentTestsRunnerDescription() {
		TestsRunnerInfo testsRunnerInfo = getCurrentTestsRunnerInfo();
		if (testsRunnerInfo != null) {
			return testsRunnerInfo.getDescription();
		} else {
			return "Select a tests runner...";
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return getCurrentTestsRunnerInfo() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute("org.eclipse.cdt.launch.TESTS_RUNNER", (String) null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String testsRunnerId = configuration.getAttribute("org.eclipse.cdt.launch.TESTS_RUNNER", (String) null);
			int comboIndex = 0;
			for (int i = 1; i < testsRunnerCombo.getItemCount(); i++) {
				TestsRunnerInfo testsRunnerInfo = getTestsRunnerInfo(i);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		TestsRunnerInfo testsRunnerInfo = getCurrentTestsRunnerInfo();
		String testsRunnerId = testsRunnerInfo != null ? testsRunnerInfo.getId() : null;
		configuration.setAttribute("org.eclipse.cdt.launch.TESTS_RUNNER", testsRunnerId);
	}

	@Override
	public String getId() {
		return TAB_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "C/C++ Testing"; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			if (getCurrentTestsRunnerInfo()==null) {
				return "Tests runner is not selected";
			}
		}
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return TestsRunnerPlugin.createAutoImage("obj16/test_notrun.gif"); //$NON-NLS-1$
	}

}

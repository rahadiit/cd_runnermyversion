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
import java.util.List;

import org.eclipse.cdt.testsrunner.model.ITestItem;

/**
 * TODO: Add comment here...
 */
public class TestPathUtils {

	private static final String TEST_PATH_PART_DELIMITER = "#"; //$NON-NLS-1$
	private static final String TEST_PATH_DELIMITER = "."; //$NON-NLS-1$
	
	public static String getTestItemPath(ITestItem testItem) {
		StringBuilder itemPath = new StringBuilder();
		List<ITestItem> parentItems = new ArrayList<ITestItem>();
		while (testItem != null) {
			parentItems.add(testItem);
			testItem = testItem.getParent();
		}
		if (!parentItems.isEmpty()) {
			for (int i = parentItems.size()-2/* exclude unnamed root test suite */; i >= 0; --i) {
				itemPath.append(parentItems.get(i).getName());
				if (i != 0) {
					itemPath.append(TEST_PATH_DELIMITER);
				}
			}
		}
		// TODO: Implement caching of the last path
		return itemPath.toString();
	}
	
	
	public static String[][] unpackTestPaths(String[] testPaths) {
		String [][] result = new String[testPaths.length][];
		for (int i = 0; i < result.length; i++) {
			result[i] = testPaths[i].split(TEST_PATH_PART_DELIMITER);
		}
		return result;
	}
	
	public static String[] packTestPaths(ITestItem[] testItems) {
		String [] result = new String[testItems.length];
		List<String> testPath = new ArrayList<String>();
		
		for (int itemIdx = 0; itemIdx < testItems.length; itemIdx++) {
			// Collect test path parts (in reverse order)
			testPath.clear();
			ITestItem item = testItems[itemIdx];
			while (item != null) {
				// Exclude root test suite
				if (item.getParent()!= null) {
					testPath.add(item.getName());
				}
				item = item.getParent();
			}
			// Join path parts into the only string
			StringBuilder sb = new StringBuilder();
			boolean needDelimiter = false;
			for (int pathPartIdx = testPath.size()-1; pathPartIdx >= 0; pathPartIdx--) {
				if (needDelimiter) {
					sb.append(TEST_PATH_PART_DELIMITER);
				} else {
					needDelimiter = true;
				}
				sb.append(testPath.get(pathPartIdx));
			}
			result[itemIdx] = sb.toString();
		}
		return result;
	}
	
}


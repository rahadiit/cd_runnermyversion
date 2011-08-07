package org.eclipse.cdt.testsrunner.internal.gtest;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;

public class GoogleTestsRunner implements ITestsRunner {

	public String[] configureLaunchParameters(String[] commandLine, String[][] testPaths) {
		final String[] gtestParameters = {
			"--gtest_repeat=1", //$NON-NLS-1$
			"--gtest_print_time=1", //$NON-NLS-1$
			"--gtest_color=no", //$NON-NLS-1$
		};
		int resultSize = commandLine.length + gtestParameters.length;

		// Build tests filter
		StringBuilder sb = null;
		if (testPaths != null && testPaths.length >= 1) {
			++resultSize;
			sb = new StringBuilder("--gtest_filter="); //$NON-NLS-1$
			boolean needTestPathDelimiter = false;
			for (String[] testPath : testPaths) {
				if (needTestPathDelimiter) {
					sb.append(":"); //$NON-NLS-1$
				} else {
					needTestPathDelimiter = true;
				}
				boolean needTestPathPartDelimiter = false;
				for (String testPathPart : testPath) {
					if (needTestPathPartDelimiter) {
						sb.append("."); //$NON-NLS-1$
					} else {
						needTestPathPartDelimiter = true;
					}
					sb.append(testPathPart);
				}
			}
		}
		
		String[] result = new String[resultSize];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(gtestParameters, 0, result, commandLine.length, gtestParameters.length);
		if (sb != null) {
			result[resultSize-1] = sb.toString();
		}
		return result;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) {
		
		try {
			OutputHandler ouputHandler = new OutputHandler(modelUpdater);
			ouputHandler.run(inputStream);
		} catch (IOException e) { // TODO: Not only IO - maybe all?
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

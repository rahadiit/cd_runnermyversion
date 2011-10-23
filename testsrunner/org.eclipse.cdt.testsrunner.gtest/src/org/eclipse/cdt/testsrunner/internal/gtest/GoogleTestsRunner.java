package org.eclipse.cdt.testsrunner.internal.gtest;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;

public class GoogleTestsRunner implements ITestsRunner {

	public String[] getAdditionalLaunchParameters(String[][] testPaths) {
		final String[] gtestParameters = {
			"--gtest_repeat=1", //$NON-NLS-1$
			"--gtest_print_time=1", //$NON-NLS-1$
			"--gtest_color=no", //$NON-NLS-1$
		};
		String[] result = gtestParameters;

		// Build tests filter
		if (testPaths != null && testPaths.length >= 1) {
			StringBuilder sb = new StringBuilder("--gtest_filter="); //$NON-NLS-1$
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
				// If it is a test suite
				if (testPath.length <= 1) {
					sb.append(".*");
				}
			}
			result = new String[gtestParameters.length + 1];
			System.arraycopy(gtestParameters, 0, result, 0, gtestParameters.length);
			result[gtestParameters.length] = sb.toString();
		}
		return result;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
		
		try {
			OutputHandler ouputHandler = new OutputHandler(modelUpdater);
			ouputHandler.run(inputStream);
		} catch (IOException e) {
			throw new TestingException("I/O Error: "+e.getLocalizedMessage());
		}
	}

}

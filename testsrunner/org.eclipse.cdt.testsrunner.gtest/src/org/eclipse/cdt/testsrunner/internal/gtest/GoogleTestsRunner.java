package org.eclipse.cdt.testsrunner.internal.gtest;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.IModelManager;

public class GoogleTestsRunner implements ITestsRunner {

	public String[] configureLaunchParameters(String[] commandLine) {
		final String[] gtestParameters = {
			"--gtest_repeat=1", //$NON-NLS-1$
			"--gtest_print_time=1", //$NON-NLS-1$
			"--gtest_color=no", //$NON-NLS-1$
		};

		String[] result = new String[commandLine.length+gtestParameters.length];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(gtestParameters, 0, result, commandLine.length, gtestParameters.length);
		return result;
	}
	
	public void run(IModelManager modelManager, InputStream inputStream) {
		
		try {
			OutputHandler ouputHandler = new OutputHandler(modelManager);
			ouputHandler.run(inputStream);
		} catch (IOException e) { // TODO: Not only IO - maybe all?
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

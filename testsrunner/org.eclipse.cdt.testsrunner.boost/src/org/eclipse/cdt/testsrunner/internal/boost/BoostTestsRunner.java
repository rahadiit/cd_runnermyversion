package org.eclipse.cdt.testsrunner.internal.boost;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.xml.sax.SAXException;

public class BoostTestsRunner implements ITestsRunner {

	public String[] configureLaunchParameters(String[] commandLine, String[][] testPaths) {
		final String[] boostParameters = {
			"--output_format=xml", //$NON-NLS-1$
			"--log_level=all", //$NON-NLS-1$
			"--report_level=no" //$NON-NLS-1$
		};
		int resultSize = commandLine.length + boostParameters.length;
		
		// Build tests filter
		// TODO: If size of testPaths > 1 -- throw an error!
		StringBuilder sb = null;
		if (testPaths != null && testPaths.length >= 1) {
			++resultSize;
			sb = new StringBuilder("--run_test="); //$NON-NLS-1$
			String[] testPath = testPaths[0];
			for (int i = 1; i < testPath.length; i++) {
				if (i != 1) {
					sb.append("/"); //$NON-NLS-1$
				}
				sb.append(testPath[i]);
			}
		}
		
		String[] result = new String[resultSize];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(boostParameters, 0, result, commandLine.length, boostParameters.length);
		if (sb != null) {
			result[resultSize-1] = sb.toString();
		}
		return result;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(inputStream, new BoostXmlLogHandler(modelUpdater));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

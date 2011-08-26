package org.eclipse.cdt.testsrunner.internal.qttest;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.xml.sax.SAXException;

public class QtTestsRunner implements ITestsRunner {

	private boolean isSpecialTestPath(String[] testPath) {
		// Root test suite should not be explicitly specified for rerun
		if (testPath.length <= 1) {
			return true;
		}
		// "initTestCase" & "cleanupTestCase" are special test case names and they should be skipped too
		String testName = testPath[testPath.length-1];
		return testName.equals("initTestCase") || testName.equals("cleanupTestCase");
	}
	
	private int getNonSpecialTestsCount(String[][] testPaths) {
		int result = 0;
		if (testPaths != null) {
			for (int i = 0; i < testPaths.length; i++) {
				String[] testPath = testPaths[i];
				result += isSpecialTestPath(testPath) ? 0 : 1;
			}
		}
		return result;
	}
	
	public String[] configureLaunchParameters(String[] commandLine, String[][] testPaths) throws TestingException {
		final String[] qtParameters = {
			"-xml", //$NON-NLS-1$
			"-flush", //$NON-NLS-1$
		};

		int testPathsLength = getNonSpecialTestsCount(testPaths);
		// If there is only special test cases were specified
		if ((testPathsLength == 0) != (testPaths.length == 0)) {
			throw new TestingException("There is no test cases to rerun (initialization and finalization test cases are not taken into account)");
		}
		String[] result = new String[commandLine.length + qtParameters.length + testPathsLength];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(qtParameters, 0, result, commandLine.length, qtParameters.length);
		// Add test filters (if necessary)
		int resultIdx = commandLine.length + qtParameters.length;
		for (int i = 0; i < testPaths.length; i++) {
			String[] testPath = testPaths[i];
			if (!isSpecialTestPath(testPath)) {
				result[resultIdx] = testPath[testPath.length-1];
				resultIdx++;
			}
		}
		return result;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(inputStream, new QtXmlLogHandler(modelUpdater));
		} catch (IOException e) {
			throw new TestingException("I/O Error: "+e.getLocalizedMessage());
			
		} catch (ParserConfigurationException e) {
			throw new TestingException("XML parse error: "+e.getLocalizedMessage());

		} catch (SAXException e) {
			throw new TestingException("XML parse error: "+e.getLocalizedMessage());
		}
	}

}

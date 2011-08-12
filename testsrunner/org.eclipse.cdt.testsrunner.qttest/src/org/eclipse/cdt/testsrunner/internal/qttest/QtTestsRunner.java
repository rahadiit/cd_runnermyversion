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

	public String[] configureLaunchParameters(String[] commandLine, String[][] testPaths) {
		final String[] qtParameters = {
			"-xml", //$NON-NLS-1$
			"-flush", //$NON-NLS-1$
		};

		int testPathsLength = testPaths != null ? testPaths.length : 0;
		String[] result = new String[commandLine.length + qtParameters.length + testPathsLength];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(qtParameters, 0, result, commandLine.length, qtParameters.length);
		// Add test filters (if necessary)
		for (int i = 0; i < testPathsLength; i++) {
			String[] testPath = testPaths[i];
			result[commandLine.length + qtParameters.length+i] = testPath[testPath.length-1];
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

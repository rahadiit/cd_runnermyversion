package org.eclipse.cdt.testsrunner.internal.qttest;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.xml.sax.SAXException;

public class QtTestsRunner implements ITestsRunner {

	public String[] configureLaunchParameters(String[] commandLine) {
		final String[] qtParameters = {
			"-xml", //$NON-NLS-1$
			"-flush", //$NON-NLS-1$
		};

		String[] result = new String[commandLine.length+qtParameters.length];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(qtParameters, 0, result, commandLine.length, qtParameters.length);
		return result;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(inputStream, new QtXmlLogHandler(modelUpdater));
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

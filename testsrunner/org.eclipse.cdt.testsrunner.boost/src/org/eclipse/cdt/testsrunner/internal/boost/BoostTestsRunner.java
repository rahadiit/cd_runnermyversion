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

	public String[] configureLaunchParameters(String[] commandLine) {
		final String[] boostParameters = {
			"--output_format=xml", //$NON-NLS-1$
			"--log_level=all", //$NON-NLS-1$
			"--report_level=no" //$NON-NLS-1$
		};

		String[] result = new String[commandLine.length+boostParameters.length];
		System.arraycopy(commandLine, 0, result, 0, commandLine.length);
		System.arraycopy(boostParameters, 0, result, commandLine.length, boostParameters.length);
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

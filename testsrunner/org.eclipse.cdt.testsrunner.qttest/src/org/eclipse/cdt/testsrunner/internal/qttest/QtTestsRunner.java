package org.eclipse.cdt.testsrunner.internal.qttest;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.IModelManager;
import org.xml.sax.SAXException;

public class QtTestsRunner implements ITestsRunner {

	public QtTestsRunner() {
		// TODO Auto-generated constructor stub
	}

	public String[] configureLaunchParameters(String[] commandLine) {
		// TODO: Implement Qt-specific options addition
		return commandLine;
	}

	public void run(IModelManager modelBuilder, InputStream inputStream) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(inputStream, new QtXmlLogHandler(modelBuilder));
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

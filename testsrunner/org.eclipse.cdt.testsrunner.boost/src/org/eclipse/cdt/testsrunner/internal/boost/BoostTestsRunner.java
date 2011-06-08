package org.eclipse.cdt.testsrunner.internal.boost;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunner;
import org.eclipse.cdt.testsrunner.model.IModelManager;
import org.xml.sax.SAXException;

public class BoostTestsRunner implements ITestsRunner {

	public void run(IModelManager modelBuilder, InputStream inputStream) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			System.out.println(">> Start...");
			sp.parse(inputStream, new BoostXmlLogHandler(modelBuilder));
			System.out.println(">> Done!");
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

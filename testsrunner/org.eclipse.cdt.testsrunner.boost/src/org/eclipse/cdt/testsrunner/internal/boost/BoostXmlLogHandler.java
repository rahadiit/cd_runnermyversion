package org.eclipse.cdt.testsrunner.internal.boost;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.testsrunner.model.IModelManager;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class BoostXmlLogHandler extends DefaultHandler {
	
	private static final String XML_NODE_TEST_LOG = "TestLog"; //$NON-NLS-1$
	private static final String XML_NODE_TEST_SUITE = "TestSuite"; //$NON-NLS-1$
	private static final String XML_NODE_TEST_CASE = "TestCase"; //$NON-NLS-1$
	private static final String XML_NODE_TESTING_TIME = "TestingTime"; //$NON-NLS-1$
	private static final String XML_NODE_LAST_CHECKPOINT = "LastCheckpoint"; //$NON-NLS-1$
	
	
	private static final String XML_NODE_INFO = "Info"; //$NON-NLS-1$
	private static final String XML_NODE_MESSAGE = "Message"; //$NON-NLS-1$
	private static final String XML_NODE_WARNING = "Warning"; //$NON-NLS-1$
	private static final String XML_NODE_ERROR = "Error"; //$NON-NLS-1$
	private static final String XML_NODE_FATAL_ERROR = "FatalError"; //$NON-NLS-1$
	private static final String XML_NODE_EXCEPTION = "Exception"; //$NON-NLS-1$
	
	
	private static final String XML_ATTR_TEST_SUITE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_TEST_CASE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_MESSAGE_FILE = "file"; //$NON-NLS-1$
	private static final String XML_ATTR_MESSAGE_LINE = "line"; //$NON-NLS-1$
	
    private static final Map<String, ITestMessage.Level> STRING_TO_MESSAGE_LEVEL;
    static {
        Map<String, ITestMessage.Level> aMap = new HashMap<String, ITestMessage.Level>();
        aMap.put(XML_NODE_INFO, ITestMessage.Level.Info);
        aMap.put(XML_NODE_MESSAGE, ITestMessage.Level.Message);
        aMap.put(XML_NODE_WARNING, ITestMessage.Level.Warning);
        aMap.put(XML_NODE_ERROR, ITestMessage.Level.Error);
        aMap.put(XML_NODE_FATAL_ERROR, ITestMessage.Level.FatalError);
        // NOTE: Exception node is processed separately
        STRING_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
    }

	private IModelManager modelManager;
	private String elementData;
	private String fileName;
	private int lineNumber;
	private ITestItem.Status testStatus;
	
	BoostXmlLogHandler(IModelManager modelBuilder) {
		this.modelManager = modelBuilder;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
		
		if (qName == XML_NODE_TEST_SUITE) {
			String testSuiteName = attrs.getValue(XML_ATTR_TEST_SUITE_NAME);
			modelManager.enterTestSuite(testSuiteName);

		} else if (qName == XML_NODE_TEST_CASE) {
			String testCaseName = attrs.getValue(XML_ATTR_TEST_CASE_NAME);
			modelManager.enterTestCase(testCaseName);
			testStatus = Status.Passed;

		} else if (STRING_TO_MESSAGE_LEVEL.containsKey(qName)
				|| qName == XML_NODE_LAST_CHECKPOINT) {
			elementData = null;
			fileName = attrs.getValue(XML_ATTR_MESSAGE_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_MESSAGE_LINE).trim());
			
		} else if (qName == XML_NODE_EXCEPTION) {
			elementData = null;
			fileName = null;
			lineNumber = -1;
			
		} else if (qName == XML_NODE_TESTING_TIME || qName == XML_NODE_TEST_LOG) {
			/* just skip, do nothing */
			
		} else {
			String message = "Invalid XML format: Element \""+qName+"\" is not accepted!";
			Activator.logErrorMessage(message);
			throw new SAXException(message);
		}
	}
	
	private void addCurrentMessage(ITestMessage.Level level) throws SAXException {
		if (elementData == null) {
			String message = "Invalid XML format: Empty message text is not accepted!";
			Activator.logErrorMessage(message);
			throw new SAXException(message);
		}
		modelManager.addTestMessage(fileName, lineNumber, level, elementData);
		elementData = null;
		fileName = null;
		lineNumber = -1;
		if (level == ITestMessage.Level.Error) {
			if (testStatus != ITestItem.Status.Aborted) {
				testStatus = ITestItem.Status.Failed;
			}
			
		} else if (level == ITestMessage.Level.FatalError || level == ITestMessage.Level.Exception) {
			testStatus = ITestItem.Status.Aborted;
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		if (qName == XML_NODE_TEST_SUITE) {
			modelManager.exitTestSuite();

		} else if (qName == XML_NODE_TEST_CASE) {
			modelManager.setTestStatus(testStatus);
			modelManager.exitTestCase();
		
		} else if (qName == XML_NODE_TESTING_TIME) {
			modelManager.setTestingTime(Integer.parseInt(elementData.trim())/100);

		} else if (STRING_TO_MESSAGE_LEVEL.containsKey(qName)) {
			addCurrentMessage(STRING_TO_MESSAGE_LEVEL.get(qName));

		} else if (qName == XML_NODE_EXCEPTION) {
			if (fileName != null && lineNumber != -1) {
				elementData += "\nSee the last checkpoint attached.";
			}
			addCurrentMessage(STRING_TO_MESSAGE_LEVEL.get(qName));

		} else if (qName == XML_NODE_TEST_LOG || qName == XML_NODE_LAST_CHECKPOINT) {
			/* just skip, do nothing */
			
		} else {
			String message = "Invalid XML format: Element \""+qName+"\" is not accepted!";
			Activator.logErrorMessage(message);
			throw new SAXException(message);
		}
	}

	public void characters(char[] ch, int start, int length) {
	    StringBuilder sb = new StringBuilder();
	    if (elementData != null) {
	    	sb.append(elementData);
	    }
		for (int i = start; i < start + length; i++) {
			sb.append(ch[i]);
		}
		elementData = sb.toString();
	}
	
	
	public void warning(SAXParseException ex) throws SAXException {
		Activator.logErrorMessage("XML warning: "+ex.getMessage()); //$NON-NLS-1$
	}

	public void error(SAXParseException ex) throws SAXException {
		Activator.logErrorMessage("XML error: "+ex.getMessage()); //$NON-NLS-1$
		throw ex;
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		Activator.logErrorMessage("XML fatal error: "+ex.getMessage()); //$NON-NLS-1$
		throw ex;
	}
	
}

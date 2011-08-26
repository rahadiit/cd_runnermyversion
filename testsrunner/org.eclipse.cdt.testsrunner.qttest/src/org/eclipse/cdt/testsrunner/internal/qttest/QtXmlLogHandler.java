package org.eclipse.cdt.testsrunner.internal.qttest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class QtXmlLogHandler extends DefaultHandler {
	
	private static final String XML_NODE_TEST_CASE = "TestCase"; //$NON-NLS-1$
	private static final String XML_NODE_TEST_FUNCTION = "TestFunction"; //$NON-NLS-1$
	private static final String XML_NODE_INCIDENT = "Incident"; //$NON-NLS-1$
	private static final String XML_NODE_MESSAGE = "Message"; //$NON-NLS-1$
	private static final String XML_NODE_DESCRIPTION = "Description"; //$NON-NLS-1$

	private static final String XML_NODE_ENVIRONMENT = "Environment"; //$NON-NLS-1$
	private static final String XML_NODE_QTVERSION = "QtVersion"; //$NON-NLS-1$
	private static final String XML_NODE_QTESTVERSION = "QTestVersion"; //$NON-NLS-1$
	private static final String XML_NODE_BENCHMARK = "BenchmarkResult"; //$NON-NLS-1$
	private static final String XML_NODE_DATATAG = "DataTag"; //$NON-NLS-1$
	
	private static final String XML_VALUE_INCIDENT_PASS = "pass"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_XFAIL = "xfail"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_FAIL = "fail"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_XPASS = "xpass"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_UNKNOWN = "??????"; //$NON-NLS-1$

	private static final String XML_VALUE_MESSAGE_WARN = "warn"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_SYSTEM = "system"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_QDEBUG = "qdebug"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_QWARN = "qwarn"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_QFATAL = "qfatal"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_SKIP = "skip"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_INFO = "info"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_UNKNOWN = "??????"; //$NON-NLS-1$

	private static final String XML_ATTR_TEST_CASE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_TEST_FUNCTION_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_TYPE = "type"; //$NON-NLS-1$
	private static final String XML_ATTR_FILE = "file"; //$NON-NLS-1$
	private static final String XML_ATTR_LINE = "line"; //$NON-NLS-1$
	
    private static final Map<String, ITestMessage.Level> STRING_TO_MESSAGE_LEVEL;
    static {
        Map<String, ITestMessage.Level> aMap = new HashMap<String, ITestMessage.Level>();
        aMap.put(XML_VALUE_MESSAGE_WARN, ITestMessage.Level.Warning);
        aMap.put(XML_VALUE_MESSAGE_SYSTEM, ITestMessage.Level.Message);
        aMap.put(XML_VALUE_MESSAGE_QDEBUG, ITestMessage.Level.Info);
        aMap.put(XML_VALUE_MESSAGE_QWARN, ITestMessage.Level.Warning);
        aMap.put(XML_VALUE_MESSAGE_QFATAL, ITestMessage.Level.FatalError);
        aMap.put(XML_VALUE_MESSAGE_SKIP, ITestMessage.Level.Info);
        aMap.put(XML_VALUE_MESSAGE_INFO, ITestMessage.Level.Info);
        aMap.put(XML_VALUE_MESSAGE_UNKNOWN, ITestMessage.Level.FatalError);
        // NOTE: Exception node is processed separately
        STRING_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
    }

    private static final Map<String, ITestCase.Status> STRING_TO_TEST_STATUS;
    static {
        Map<String, ITestCase.Status> aMap = new HashMap<String, ITestCase.Status>();
        aMap.put(XML_VALUE_INCIDENT_PASS, ITestCase.Status.Passed);
        aMap.put(XML_VALUE_INCIDENT_XFAIL, ITestCase.Status.Aborted);
        aMap.put(XML_VALUE_INCIDENT_FAIL, ITestCase.Status.Aborted);
        aMap.put(XML_VALUE_INCIDENT_XPASS, ITestCase.Status.Passed);
        aMap.put(XML_VALUE_INCIDENT_UNKNOWN, ITestCase.Status.Aborted);
        // NOTE: Exception node is processed separately
        STRING_TO_TEST_STATUS = Collections.unmodifiableMap(aMap);
    }

    private static final Map<String, ITestMessage.Level> STRING_INCIDENT_TO_MESSAGE_LEVEL;
    static {
        Map<String, ITestMessage.Level> aMap = new HashMap<String, ITestMessage.Level>();
        aMap.put(XML_VALUE_INCIDENT_PASS, ITestMessage.Level.Info);
        aMap.put(XML_VALUE_INCIDENT_XFAIL, ITestMessage.Level.FatalError);
        aMap.put(XML_VALUE_INCIDENT_FAIL, ITestMessage.Level.FatalError);
        aMap.put(XML_VALUE_INCIDENT_XPASS, ITestMessage.Level.Info);
        aMap.put(XML_VALUE_INCIDENT_UNKNOWN, ITestMessage.Level.FatalError);
        // NOTE: Exception node is processed separately
        STRING_INCIDENT_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
    }

	private ITestModelUpdater modelUpdater;
	private String elementData;
	private String messageText;
	private String fileName;
	private int lineNumber;
	private ITestMessage.Level messageLevel;
	private long testStartTime;
	
	QtXmlLogHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
		
		elementData = null;
		if (qName == XML_NODE_TEST_CASE) {
			// NOTE: Terminology mapping: Qt Test Case is actually a Test Suite
			String testSuiteName = attrs.getValue(XML_ATTR_TEST_CASE_NAME);
			modelUpdater.enterTestSuite(testSuiteName);

		} else if (qName == XML_NODE_TEST_FUNCTION) {
			// NOTE: Terminology mapping: Qt Test Function is actually a Test Case
			String testCaseName = attrs.getValue(XML_ATTR_TEST_FUNCTION_NAME);
			modelUpdater.enterTestCase(testCaseName);
			testStartTime = System.currentTimeMillis();

		} else if (qName == XML_NODE_MESSAGE) {
			fileName = attrs.getValue(XML_ATTR_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_LINE).trim());
			messageLevel = STRING_TO_MESSAGE_LEVEL.get(attrs.getValue(XML_ATTR_TYPE));			
			messageText = null;

		} else if (qName == XML_NODE_INCIDENT) {
			fileName = attrs.getValue(XML_ATTR_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_LINE).trim());
			String strType = attrs.getValue(XML_ATTR_TYPE);
			messageLevel = STRING_INCIDENT_TO_MESSAGE_LEVEL.get(strType);
			modelUpdater.setTestStatus(STRING_TO_TEST_STATUS.get(strType));
			messageText = null;

		} else if (qName == XML_NODE_DESCRIPTION
				|| qName == XML_NODE_ENVIRONMENT
				|| qName == XML_NODE_QTVERSION
				|| qName == XML_NODE_QTESTVERSION
				|| qName == XML_NODE_BENCHMARK
				|| qName == XML_NODE_DATATAG) {
			/* just skip, do nothing */

		} else {
			String message = "Invalid XML format: Element \""+qName+"\" is not accepted!";
			Activator.logErrorMessage(message);
			throw new SAXException(message);
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		if (qName == XML_NODE_TEST_CASE) {
			modelUpdater.exitTestSuite();

		} else if (qName == XML_NODE_TEST_FUNCTION) {
			modelUpdater.setTestingTime((int)(System.currentTimeMillis()-testStartTime));
			testStartTime = 0;
			modelUpdater.exitTestCase();
		
		} else if (qName == XML_NODE_MESSAGE || qName == XML_NODE_INCIDENT) {
			if (messageText != null) {
				modelUpdater.addTestMessage(fileName, lineNumber, messageLevel, messageText);
			}

		} else if (qName == XML_NODE_DESCRIPTION) {
			messageText = elementData.isEmpty() ? null : elementData;

		} else if (qName == XML_NODE_ENVIRONMENT
				|| qName == XML_NODE_QTVERSION
				|| qName == XML_NODE_QTESTVERSION
				|| qName == XML_NODE_BENCHMARK
				|| qName == XML_NODE_DATATAG) {
			/* just skip, do nothing */
			
		} else {
			String message = "Invalid XML format: Element \""+qName+"\" is not accepted!";
			Activator.logErrorMessage(message);
			throw new SAXException(message);
		}
		elementData = null;
	}

	public void characters(char[] ch, int start, int length) {
	    StringBuilder sb = new StringBuilder();
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

package org.eclipse.cdt.testsrunner.internal.qttest;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
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
	private static final String XML_ATTR_BENCHMARK_METRIC = "metric"; //$NON-NLS-1$
	private static final String XML_ATTR_BENCHMARK_VALUE = "value"; //$NON-NLS-1$
	private static final String XML_ATTR_BENCHMARK_ITERATIONS = "iterations"; //$NON-NLS-1$
	private static final String XML_ATTR_DATA_TAG = "tag";  //$NON-NLS-1$
	
    private static final Map<String, ITestMessage.Level> STRING_TO_MESSAGE_LEVEL;
    static {
        Map<String, ITestMessage.Level> aMap = new HashMap<String, ITestMessage.Level>();
        aMap.put(XML_VALUE_MESSAGE_WARN, ITestMessage.Level.Warning);
        aMap.put(XML_VALUE_MESSAGE_SYSTEM, ITestMessage.Level.Message);
        aMap.put(XML_VALUE_MESSAGE_QDEBUG, ITestMessage.Level.Message);
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
        aMap.put(XML_VALUE_INCIDENT_XFAIL, ITestCase.Status.Failed);
        aMap.put(XML_VALUE_INCIDENT_FAIL, ITestCase.Status.Failed);
        aMap.put(XML_VALUE_INCIDENT_XPASS, ITestCase.Status.Failed);
        aMap.put(XML_VALUE_INCIDENT_UNKNOWN, ITestCase.Status.Aborted);
        // NOTE: Exception node is processed separately
        STRING_TO_TEST_STATUS = Collections.unmodifiableMap(aMap);
    }

    private static final Map<String, ITestMessage.Level> STRING_INCIDENT_TO_MESSAGE_LEVEL;
    static {
        Map<String, ITestMessage.Level> aMap = new HashMap<String, ITestMessage.Level>();
        aMap.put(XML_VALUE_INCIDENT_PASS, ITestMessage.Level.Info);
        aMap.put(XML_VALUE_INCIDENT_XFAIL, ITestMessage.Level.Error);
        aMap.put(XML_VALUE_INCIDENT_FAIL, ITestMessage.Level.FatalError);
        aMap.put(XML_VALUE_INCIDENT_XPASS, ITestMessage.Level.Error);
        aMap.put(XML_VALUE_INCIDENT_UNKNOWN, ITestMessage.Level.FatalError);
        // NOTE: Exception node is processed separately
        STRING_INCIDENT_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
    }

    private static final Map<String, String> XML_METRICS_TO_UNIT_NAME;
    static {
        Map<String,String> aMap = new HashMap<String, String>();
        aMap.put("events", "events");
        aMap.put("callgrind", "instr.");
        aMap.put("walltime", "msec");
        aMap.put("cputicks", "ticks");
        // NOTE: Exception node is processed separately
        XML_METRICS_TO_UNIT_NAME = Collections.unmodifiableMap(aMap);
    }

	private ITestModelUpdater modelUpdater;
	private String elementData;
	private String messageText;
	private String fileName;
	private int lineNumber;
	private ITestMessage.Level messageLevel;
	private ITestItem.Status testCaseStatus;
	private String testCaseName;
	private String currentDataTag;
	private String lastDataTag;
	private boolean testCaseAdded;
	
	QtXmlLogHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}

	private void exitTestCaseIfNecessary() {
		if (testCaseAdded) {
			modelUpdater.setTestStatus(testCaseStatus);
			modelUpdater.exitTestCase();
			testCaseAdded = false;
		}
	}
	
	private void createTestCaseIfNecessary() {
		if (!lastDataTag.equals(currentDataTag)) {
			exitTestCaseIfNecessary();
			currentDataTag = lastDataTag;
			String suffix = !currentDataTag.equals("") ? "("+currentDataTag+")" : "";
			modelUpdater.enterTestCase(testCaseName+suffix);
			testCaseAdded = true;
		}
	}
	
	private void addTestMessageIfNecessary() {
		if (messageText != null) {
			modelUpdater.addTestMessage(fileName, lineNumber, messageLevel, messageText);
		}
	}
	
	private void setCurrentTestCaseStatus(ITestItem.Status newStatus) {
		// NOTE: Passed status is set by default and should not be set explicitly.
		//       But in case of errors it should not override Failed or Skipped statuses.
		if (newStatus != ITestItem.Status.Passed) {
			testCaseStatus = newStatus;
		}
	}
	
	private String getUnitsByBenchmarkMetric(String benchmarkMetric) throws SAXException {
		String units = XML_METRICS_TO_UNIT_NAME.get(benchmarkMetric);
		if (units == null) {
			logAndThrowError("Benchmarck metric value \""+benchmarkMetric+"\" is not supported!");
		}
		return units;
	}
	
	private ITestMessage.Level getMessageLevel(Map<String, ITestMessage.Level> map, String incidentTypeStr) throws SAXException {
		Level result = map.get(incidentTypeStr);
		if (result == null) {
			logAndThrowError("String \""+incidentTypeStr+"\" cannot be converted to a message level!");
		}
		return result;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
		
		elementData = null;
		if (qName == XML_NODE_TEST_CASE) {
			// NOTE: Terminology mapping: Qt Test Case is actually a Test Suite
			String testSuiteName = attrs.getValue(XML_ATTR_TEST_CASE_NAME);
			modelUpdater.enterTestSuite(testSuiteName);

		} else if (qName == XML_NODE_TEST_FUNCTION) {
			// NOTE: Terminology mapping: Qt Test Function is actually a Test Case
			testCaseName = attrs.getValue(XML_ATTR_TEST_FUNCTION_NAME);
			currentDataTag = null;
			lastDataTag = "";
			testCaseAdded = false;
			testCaseStatus = ITestItem.Status.Passed;

		} else if (qName == XML_NODE_MESSAGE) {
			String messageLevelStr = attrs.getValue(XML_ATTR_TYPE);
			fileName = attrs.getValue(XML_ATTR_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_LINE).trim());
			messageLevel = getMessageLevel(STRING_TO_MESSAGE_LEVEL, messageLevelStr);			
			messageText = null;
			if (messageLevelStr.equals(XML_VALUE_MESSAGE_SKIP)) {
				setCurrentTestCaseStatus(ITestCase.Status.Skipped);
			}

		} else if (qName == XML_NODE_INCIDENT) {
			String strType = attrs.getValue(XML_ATTR_TYPE);
			fileName = attrs.getValue(XML_ATTR_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_LINE).trim());
			messageLevel = getMessageLevel(STRING_INCIDENT_TO_MESSAGE_LEVEL, strType);
			messageText = null;
			setCurrentTestCaseStatus(STRING_TO_TEST_STATUS.get(strType));

		} else if (qName == XML_NODE_BENCHMARK) {
			lastDataTag = attrs.getValue(XML_ATTR_DATA_TAG);
			createTestCaseIfNecessary();
			int benchmarkResultIteratations = Integer.parseInt(attrs.getValue(XML_ATTR_BENCHMARK_ITERATIONS).trim());
			float benchmarkResultValue = Integer.parseInt(attrs.getValue(XML_ATTR_BENCHMARK_VALUE).trim());
			String units = getUnitsByBenchmarkMetric(attrs.getValue(XML_ATTR_BENCHMARK_METRIC).trim());
			modelUpdater.addTestMessage("", 0, ITestMessage.Level.Info,
				MessageFormat.format("{0,number,#.####} {1} per iteration (total: {2}, iterations: {3})", 
					benchmarkResultValue/benchmarkResultIteratations, units, benchmarkResultValue, benchmarkResultIteratations
				)
			);

		} else if (qName == XML_NODE_DATATAG) {
			lastDataTag = "";

		} else if (qName == XML_NODE_DESCRIPTION
				|| qName == XML_NODE_ENVIRONMENT
				|| qName == XML_NODE_QTVERSION
				|| qName == XML_NODE_QTESTVERSION) {
			/* just skip, do nothing */

		} else {
			logAndThrowErrorForElement(qName);
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		if (qName == XML_NODE_TEST_CASE) {
			modelUpdater.exitTestSuite();
			
		} else if (qName == XML_NODE_TEST_FUNCTION) {
			createTestCaseIfNecessary();
			exitTestCaseIfNecessary();

		} else if (qName == XML_NODE_DATATAG) {
			lastDataTag = elementData;
			
		} else if (qName == XML_NODE_INCIDENT) {
			createTestCaseIfNecessary();
			addTestMessageIfNecessary();

		} else if (qName == XML_NODE_MESSAGE) {
			createTestCaseIfNecessary();
			addTestMessageIfNecessary();

		} else if (qName == XML_NODE_DESCRIPTION) {
			messageText = elementData == null || elementData.isEmpty() ? "" : elementData;

		} else if (qName == XML_NODE_ENVIRONMENT
				|| qName == XML_NODE_QTVERSION
				|| qName == XML_NODE_QTESTVERSION
				|| qName == XML_NODE_BENCHMARK) {
			/* just skip, do nothing */
			
		} else {
			logAndThrowErrorForElement(qName);
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
	

	private void logAndThrowErrorForElement(String tagName) throws SAXException {
		logAndThrowError("Invalid XML format: Element \""+tagName+"\" is not accepted!");
	}
	
	private void logAndThrowError(String message) throws SAXException {
		Activator.logErrorMessage(message);
		throw new SAXException(message);
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

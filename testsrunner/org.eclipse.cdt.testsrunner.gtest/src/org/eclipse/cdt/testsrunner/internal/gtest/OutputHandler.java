package org.eclipse.cdt.testsrunner.internal.gtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.TestingException;


public class OutputHandler {
	
	class State {
		private Pattern enterPattern;
		private Matcher matcher;
		private int groupCount;
		
		State(String enterRegex) {
			this(enterRegex, -1);
		}
		
		State(String enterRegex, int groupCount) {
			enterPattern = Pattern.compile(enterRegex);
			this.groupCount = groupCount;
		}
		
		public boolean match(String line) throws TestingException {
			matcher = enterPattern.matcher(line);
			boolean groupsCountOk = groupCount == -1 || matcher.groupCount() == groupCount;
			if (!groupsCountOk) {
				generateInternalError(
					MessageFormat.format(
						"State with pattern \"{0}\" should has {1} groups but has {2}.",
						enterPattern.pattern(), matcher.groupCount(), groupCount 
					)
				);
			}
			boolean matches = matcher.matches();
			if (!matches || !groupsCountOk) {
				// Do not keep the reference - it will be unnecessary anyway
				matcher = null;
			}
			return matches;
		}
		
		protected String group(int groupNumber) {
			return matcher.group(groupNumber);
		}
		
		public void onEnter(State previousState) throws TestingException {}

		public void onExit(State nextState) {}
		
		protected String getTestSuiteName(String name, String typeParameter) {
			return (typeParameter != null) ? MessageFormat.format("{0}({1})", name, typeParameter.trim()) : name; //$NON-NLS-1$
		}
	}
	
	
	class TestSuiteStart extends State {
		private String typeParameter;
		
		TestSuiteStart(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		public void onEnter(State previousState) {
			typeParameter = group(3);
			modelUpdater.enterTestSuite(getTestSuiteName(group(1), typeParameter));
		}
		
		public String getTypeParameter() {
			return typeParameter;
		}
	}
	
	class TestCaseStart extends State {
		TestCaseStart(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		public void onEnter(State previousState) throws TestingException {
			String testCaseName = group(2);
			String lastTestSuiteName = modelUpdater.currentTestSuite().getName();
			String currTestSuiteName = getTestSuiteName(group(1), stateTestSuiteStart.getTypeParameter());
			if (!lastTestSuiteName.equals(currTestSuiteName)) {
				generateInternalError(
					MessageFormat.format(
						"A test case \"{0}\" belongs to test suite \"{1}\", but the last started suite is \"{2}\".",
						testCaseName, currTestSuiteName, lastTestSuiteName
					)
				);
			}
			modelUpdater.enterTestCase(testCaseName);
		}
	}
	
	class ErrorMessageLocation extends State {
		private String messageFileName;
		private int messageLineNumber;
		private String messagePart;
		
		ErrorMessageLocation(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}
		
		public void onEnter(State previousState) throws TestingException {
			String fileNameIfLinePresent = group(2);
			String fileNameIfLineAbsent = group(6);
			String lineNumberCommon = group(4);
			String lineNumberVS = group(5);
			if (fileNameIfLinePresent != null) {
				if (lineNumberCommon != null) {
					messageFileName = fileNameIfLinePresent;
					messageLineNumber = Integer.parseInt(lineNumberCommon.trim());
				} else if (lineNumberVS != null) {
					messageFileName = fileNameIfLinePresent;
					messageLineNumber = Integer.parseInt(lineNumberVS.trim());
				} else {
					if (!modelUpdater.currentTestSuite().getName().equals(group(1))) {
						generateInternalError("Unknown location format.");
					}
				}
			} else if (fileNameIfLineAbsent != null) {
				if (lineNumberCommon == null && lineNumberVS == null) {
					messageFileName = fileNameIfLineAbsent;
					messageLineNumber = DEFAULT_LOCATION_LINE;
				} else {
					generateInternalError("Unknown location format.");
				}
			}
			// Check special case when file is not known - reset location
			if (messageFileName.equals("unknown file")) { //$NON-NLS-1$
				messageFileName = DEFAULT_LOCATION_FILE;
			}
			// NOTE: For Visual Studio style there is also first part of the message at this line
			messagePart = group(8);
		}
		
		public String getMessageFileName() {
			return messageFileName;
		}
		
		public int getMessageLineNumber() {
			return messageLineNumber;
		}
		
		public String getMessagePart() {
			return messagePart;
		}
	}
	
	class ErrorMessage extends State {

		private StringBuilder messagePart = new StringBuilder();

		ErrorMessage(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		public void onEnter(State previousState) {
			boolean needEndOfLine = (this == previousState);
			if (this != previousState) {
				String firstMessagePart = stateErrorMessageLocation.getMessagePart();
				if (firstMessagePart != null) {
					messagePart.append(firstMessagePart);
					needEndOfLine = true;
				}
			}
			if (needEndOfLine) {
				messagePart.append(System.getProperty("line.separator")); //$NON-NLS-1$
			}
			messagePart.append(group(1));
		}

		public void onExit(State nextState) {
			if (this != nextState) {
				modelUpdater.addTestMessage(
					stateErrorMessageLocation.getMessageFileName(),
					stateErrorMessageLocation.getMessageLineNumber(),
					ITestMessage.Level.Error,
					messagePart.toString()
				);
				messagePart.setLength(0);
			}
		}
	}
	
	class TestTrace extends ErrorMessageLocation {
		
		TestTrace(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		public void onEnter(State previousState) throws TestingException {
			super.onEnter(previousState);
			modelUpdater.addTestMessage(
				getMessageFileName(),
				getMessageLineNumber(),
				ITestMessage.Level.Info,
				getMessagePart()
			);
		}

		
	}
	
	class TestCaseEnd extends State {

		TestCaseEnd(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}
		
		public void onEnter(State previousState) throws TestingException {
			String lastTestSuiteName = modelUpdater.currentTestSuite().getName();
			String explicitTypeParameter = group(5); 
			String typeParameter = explicitTypeParameter != null ? explicitTypeParameter : stateTestSuiteStart.getTypeParameter(); 
			String currTestSuiteName = getTestSuiteName(group(2), typeParameter);
			if (!lastTestSuiteName.equals(currTestSuiteName)) {
				generateInternalError(
					MessageFormat.format(
						"A test case \"{0}\" belongs to test suite \"{1}\", but the last started suite is \"{2}\".",
						group(2), currTestSuiteName, lastTestSuiteName
					)
				);					
			}
			String lastTestCaseName = modelUpdater.currentTestCase().getName();
			if (!lastTestCaseName.equals(group(3))) {
				generateInternalError(
						MessageFormat.format(
							"End of test case \"{0}\" is not expected, because the last started case is \"{1}\".",
							group(3), lastTestCaseName
						)
					);					
			}
			String testStatusStr = group(1);
			ITestItem.Status testStatus = ITestItem.Status.Skipped;
			if (testStatusStr.equals(testStatusOk)) {
				testStatus = ITestItem.Status.Passed;
			} else if (testStatusStr.equals(testStatusFailed)) {
				testStatus = ITestItem.Status.Failed;
			} else {
				generateInternalError(MessageFormat.format("Test status value \"{0}\" is unknown.", testStatusStr));
			}
			String getParamValue = group(7);
			if (getParamValue != null) {
				modelUpdater.addTestMessage(
						DEFAULT_LOCATION_FILE,
						DEFAULT_LOCATION_LINE,
						ITestMessage.Level.Info,
						MessageFormat.format("Instantiated with GetParam() = {0}", getParamValue)
					);
				
			}
			modelUpdater.setTestingTime(Integer.parseInt(group(8)));
			modelUpdater.setTestStatus(testStatus);
			modelUpdater.exitTestCase();
		}
	}
	
	class TestSuiteEnd extends State {

		TestSuiteEnd(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}
		
		public void onEnter(State previousState) throws TestingException {
			String lastTestSuiteName = modelUpdater.currentTestSuite().getName();
			String currTestSuiteName = getTestSuiteName(group(1), stateTestSuiteStart.getTypeParameter());
			if (!lastTestSuiteName.equals(currTestSuiteName)) {
				generateInternalError(
					MessageFormat.format(
						"End of test suite \"{0}\" is not expected, because the last started suite is \"{1}\".",
						currTestSuiteName, lastTestSuiteName
					)
				);					
			}
			modelUpdater.exitTestSuite();
		}
	}
	
	
	private ITestModelUpdater modelUpdater;

	// Common regular expression parts
	static private String regexTestSuiteName = "([^,]+)"; //$NON-NLS-1$
	static private String regexParameterInstantiation = "(\\s*,\\s+where\\s+TypeParam\\s*=([^,(]+))?"; //$NON-NLS-1$
	static private String regexTestName = regexTestSuiteName+"\\.([^,]+)"; //$NON-NLS-1$
	static private String regexTestCount = "\\d+\\s+tests?"; //$NON-NLS-1$
	static private String regexTestTime = "(\\d+)\\s+ms"; //$NON-NLS-1$
	/* Matches location in the following formats:
	 *   - /file:line:
	 *   - /file(line):
	 *   - /file:       (with no line number specified)
	 * Groups:
	 *   1 - all except ":"
	 *   2 - file name (if line present) *
	 *   3 - line number with delimiters
	 *   4 - line number (common style) *
	 *   5 - line number (Visual Studio style) *
	 *   6 - file name (if no line number specified) *
	 * Using: 
	 *   - group 2 with 4 or 5 (if line number was specified)
	 *   - group 6 (if filename only was specified)
	 */
	static private String regexLocation = "((.*)(:(\\d+)|\\((\\d+)\\))|(.*[^):])):"; //$NON-NLS-1$
	
	static private String testStatusOk = "OK"; //$NON-NLS-1$
	static private String testStatusFailed = "FAILED"; //$NON-NLS-1$
	

	// All available states
	private State stateInitial = new State(""); //$NON-NLS-1$
	private State stateInitialized = new State(".*Global test environment set-up.*"); //$NON-NLS-1$
	private TestSuiteStart stateTestSuiteStart = new TestSuiteStart("\\[-*\\]\\s+"+regexTestCount+"\\s+from\\s+"+regexTestSuiteName+regexParameterInstantiation, 3); //$NON-NLS-1$ //$NON-NLS-2$
	private State stateTestCaseStart = new TestCaseStart("\\[\\s*RUN\\s*\\]\\s+"+regexTestName, 2); //$NON-NLS-1$
	private ErrorMessageLocation stateErrorMessageLocation = new ErrorMessageLocation(regexLocation+"\\s+(Failure|error: (.*))", 8); //$NON-NLS-1$
	private State stateErrorMessage = new ErrorMessage("(.*)", 1); //$NON-NLS-1$
	private State stateTestTraceStart = new State(".*Google Test trace.*"); //$NON-NLS-1$
	// NOTE: Use 8 groups instead of 7 cause we need to be consistent with ErrorMessageLocation (as we subclass it)
	private State stateTestTrace = new TestTrace(regexLocation+"\\s+((.*))", 8); //$NON-NLS-1$
	private State stateTestCaseEnd = new TestCaseEnd("\\[\\s*("+testStatusOk+"|"+testStatusFailed+")\\s*\\]\\s+"+regexTestName+regexParameterInstantiation+"(\\s*,\\s+where\\s+GetParam\\s*\\(\\s*\\)\\s*=\\s*(.+))?\\s+\\("+regexTestTime+"\\)", 8); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private State stateTestSuiteEnd = new TestSuiteEnd("\\[-*\\]\\s+"+regexTestCount+"\\s+from\\s+"+regexTestSuiteName+"\\s+\\("+regexTestTime+"\\s+total\\)", 2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private State stateFinal = new State(".*Global test environment tear-down.*"); //$NON-NLS-1$
	// NOTE: This state is a special workaround for empty test modules (they haven't got global test environment set-up/tear-down). They should be always passed.
	private State stateEmptyTestModuleFinal = new State(".*\\[\\s*PASSED\\s*\\]\\s+0\\s+tests.*"); //$NON-NLS-1$
	
	private State currentState;
	private Map<State, State[] > transitions = new HashMap<State, State[]>();
	{
		// NOTE: Next states order is important!
		transitions.put(from(stateInitial), to(stateInitialized, stateEmptyTestModuleFinal));
		transitions.put(from(stateInitialized), to(stateTestSuiteStart));
		transitions.put(from(stateTestSuiteStart), to(stateTestCaseStart));
		transitions.put(from(stateTestCaseStart), to(stateTestCaseEnd, stateErrorMessageLocation));
		transitions.put(from(stateErrorMessageLocation), to(stateTestTraceStart, stateTestCaseEnd, stateErrorMessageLocation, stateErrorMessage));
		transitions.put(from(stateErrorMessage), to(stateTestTraceStart, stateTestCaseEnd, stateErrorMessageLocation, stateErrorMessage));
		transitions.put(from(stateTestTraceStart), to(stateTestTrace));
		transitions.put(from(stateTestTrace), to(stateTestCaseEnd, stateErrorMessageLocation, stateTestTrace));
		transitions.put(from(stateTestCaseEnd), to(stateTestCaseStart, stateTestSuiteEnd));
		transitions.put(from(stateTestSuiteEnd), to(stateTestSuiteStart, stateFinal));
	}
	
	private static final String DEFAULT_LOCATION_FILE = null;
	private static final int DEFAULT_LOCATION_LINE = 1;
    
	
	OutputHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}
	
	public void run(InputStream inputStream) throws IOException, TestingException {
		// Initialize input stream reader
		InputStreamReader streamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(streamReader);
        String line;
        boolean finalizedProperly = false;

        // Initialize internal state
		currentState = stateInitial;
        while ( ( line = reader.readLine() ) != null ) {
        	// Search for the next possible state
        	State[] possibleNextStates = transitions.get(currentState);
        	if (possibleNextStates == null) {
        		// Final state, stop running
        		finalizedProperly = true;
        		break;
        	}
        	for (State nextState : possibleNextStates) {
        		if (nextState.match(line)) {
        			// Next state found - send notifications to the states 
        			currentState.onExit(nextState);
        			State previousState = currentState;
        			currentState = nextState;
        			nextState.onEnter(previousState);
        			break;
        		}
        	}
        	// NOTE: We cannot be sure that we cover all the output of gtest with our regular expressions
        	//       (e.g. some framework notes or warnings may be uncovered etc.), so we just skip unmatched 
        	//       lines without an error
        }
        // Check whether the last line leads to the final state
        if (transitions.get(currentState) == null) {
    		finalizedProperly = true;
        }
        if (!finalizedProperly) {
        	generateInternalError("Unexpected test module output.");
        }
	}
	
	private void generateInternalError(String additionalInfo) throws TestingException
	{
		throw new TestingException("Unknown error during parsing Google Test module output: "+additionalInfo);
	}

	private State from(State fromState) {
		return fromState;
	}
	
	private State[] to(State... toStates) {
		return toStates;
	}
	
}

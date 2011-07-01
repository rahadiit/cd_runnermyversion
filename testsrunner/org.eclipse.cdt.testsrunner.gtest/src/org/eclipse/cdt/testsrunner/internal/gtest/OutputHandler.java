package org.eclipse.cdt.testsrunner.internal.gtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.testsrunner.model.IModelManager;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;


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
		
		public boolean match(String line) {
			matcher = enterPattern.matcher(line);
			boolean matches = matcher.matches() && (groupCount == -1 || matcher.groupCount() == groupCount);
			if (!matches) {
				// Do not keep the reference - it will be unnecessary anyway
				matcher = null;
			}
			return matches;
		}
		
		protected String group(int groupNumber) {
			return matcher.group(groupNumber);
		}
		
		public void onEnter(State previousState) {}

		public void onExit(State nextState) {}
	}
	
	
	class TestSuiteStart extends State {
		TestSuiteStart(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		public void onEnter(State previousState) {
			modelManager.enterTestSuite(group(1));
		}
	}
	
	class TestCaseStart extends State {
		TestCaseStart(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		public void onEnter(State previousState) {
			// TODO: Check: current TS == group(1)
			//if (modelManager.currentTestSuite().getName().equals(group(1))) ...
			modelManager.enterTestCase(group(2));
		}
	}
	
	class ErrorMessageLocation extends State {
		private String messageFileName;
		private int messageLineNumber;
		private String messagePart;
		
		ErrorMessageLocation(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}
		
		public void onEnter(State previousState) {
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
					// TODO: Internal error: unknown location format!
				}
			} else if (fileNameIfLineAbsent != null) {
				if (lineNumberCommon == null && lineNumberVS == null) {
					messageFileName = fileNameIfLineAbsent;
					messageLineNumber = 1;
				} else {
					// TODO: Internal error: unknown location format!
				}
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
				modelManager.addTestMessage(
					stateErrorMessageLocation.getMessageFileName(),
					stateErrorMessageLocation.getMessageLineNumber(),
					ITestMessage.Level.Error,
					messagePart.toString()
				);
				messagePart.setLength(0);
			}
		}
	}
	
	class TestCaseEnd extends State {

		TestCaseEnd(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}
		
		public void onEnter(State previousState) {
			// TODO: Check: current TS == group(2)
			// TODO: Check: current TC == group(3)
			String testStatusStr = group(1);
			ITestItem.Status testStatus = ITestItem.Status.Skipped;
			if (testStatusStr.equals(testStatusOk)) {
				testStatus = ITestItem.Status.Passed;
			} else if (testStatusStr.equals(testStatusFailed)) {
				testStatus = ITestItem.Status.Failed;
			} else {
				// TODO: Format error!
			}
			modelManager.setTestingTime(Integer.parseInt(group(5)));
			modelManager.setTestStatus(testStatus);
			modelManager.exitTestCase();
		}
	}
	
	class TestSuiteEnd extends State {

		TestSuiteEnd(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}
		
		public void onEnter(State previousState) {
			// TODO: Check: current TS == group(1)
			modelManager.exitTestSuite();
		}
	}
	
	
	private IModelManager modelManager;

	// Common regular expression parts
	static private String regexTestSuiteName = "([^,]+)"; //$NON-NLS-1$
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
	private State stateTestSuiteStart = new TestSuiteStart("\\[-*\\]\\s+"+regexTestCount+"\\s+from\\s+"+regexTestSuiteName+"(|,.+)", 2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private State stateTestCaseStart = new TestCaseStart("\\[\\s*RUN\\s*\\]\\s+"+regexTestName, 2); //$NON-NLS-1$
	private ErrorMessageLocation stateErrorMessageLocation = new ErrorMessageLocation(regexLocation+"\\s+(Failure|error: (.*))", 8); //$NON-NLS-1$
	private State stateErrorMessage = new ErrorMessage("(.*)", 1); //$NON-NLS-1$
	private State stateTestCaseEnd = new TestCaseEnd("\\[\\s*("+testStatusOk+"|"+testStatusFailed+")\\s*\\]\\s+"+regexTestName+"(|,.+)\\s+\\("+regexTestTime+"\\)", 5); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private State stateTestSuiteEnd = new TestSuiteEnd("\\[-*\\]\\s+"+regexTestCount+"\\s+from\\s+"+regexTestSuiteName+"\\s+\\("+regexTestTime+"\\s+total\\)", 2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private State stateFinal = new State(".*Global test environment tear-down.*"); //$NON-NLS-1$
	
	private State currentState;
	private Map<State, State[] > transitions = new HashMap<State, State[]>();
	{
		// NOTE: Next states order is important!
		transitions.put(from(stateInitial), to(stateInitialized));
		transitions.put(from(stateInitialized), to(stateTestSuiteStart));
		transitions.put(from(stateTestSuiteStart), to(stateTestCaseStart));
		transitions.put(from(stateTestCaseStart), to(stateTestCaseEnd, stateErrorMessageLocation));
		transitions.put(from(stateErrorMessageLocation), to(stateErrorMessage));
		transitions.put(from(stateErrorMessage), to(stateTestCaseEnd, stateErrorMessageLocation, stateErrorMessage));
		transitions.put(from(stateTestCaseEnd), to(stateTestCaseStart, stateTestSuiteEnd));
		transitions.put(from(stateTestSuiteEnd), to(stateTestSuiteStart, stateFinal));
		checkStateMachine();
	}
	
	
	OutputHandler(IModelManager modelManager) {
		this.modelManager = modelManager;
	}
	
	public void run(InputStream inputStream) throws IOException {
		// Initialize input stream reader
		InputStreamReader streamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(streamReader);
        String line;

        // Initialize internal state
		currentState = stateInitial;
        while ( ( line = reader.readLine() ) != null ) {
        	// Search for the next possible state
        	State[] possibleNextStates = transitions.get(currentState);
        	if (possibleNextStates == null) {
        		// Final state, stop running
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
	}

	private boolean checkStateMachine() {
		// Check if only final state has no transitions
		for (State state : transitions.keySet()) {
			if (transitions.get(state).length == 0 && state != stateFinal) {
				// TODO: Internal error!
				return false;
			}
		}
		return true;
	}
	
	private State from(State fromState) {
		return fromState;
	}
	
	private State[] to(State... toStates) {
		return toStates;
	}
	
}

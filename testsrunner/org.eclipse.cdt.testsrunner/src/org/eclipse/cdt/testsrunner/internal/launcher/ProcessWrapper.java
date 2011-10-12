package org.eclipse.cdt.testsrunner.internal.launcher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wraps the underline process and prevent accessing to its output or error stream.
 * This wrapping is necessary to prevent handling the test module output by Console
 * because we want to handle it here.
 * 
 */
class ProcessWrapper extends Process {
	
	private Process wrappedProcess;
	
	private boolean hideInputStream;
	private boolean hideErrorStream;
	

	private byte buffer[] = new byte[0];
	private InputStream emptyInputStream = new ByteArrayInputStream(buffer);
	
	private Object waitForSync = new Object();
	private boolean streamsClosingIsAllowed = false;

	
	ProcessWrapper(Process wrappedProcess, boolean hideInputStream, boolean hideErrorStream) {
		this.wrappedProcess = wrappedProcess;
		this.hideInputStream = hideInputStream;
		this.hideErrorStream = hideErrorStream;
	}
	
	@Override
	public void destroy() {
		wrappedProcess.destroy();
	}

	@Override
	public int exitValue() {
		return wrappedProcess.exitValue();
	}

	@Override
	public InputStream getErrorStream() {
		return hideErrorStream ? emptyInputStream : wrappedProcess.getErrorStream();
	}

	@Override
	public InputStream getInputStream() {
		return hideInputStream ? emptyInputStream : wrappedProcess.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return wrappedProcess.getOutputStream();
	}

	@Override
	public int waitFor() throws InterruptedException {
		// NOTE: implementation of waitFor() in Spawner will close streams after process is terminated,
		// so we should wait with this operation until we process all stream data
		synchronized (waitForSync) {
			if (!streamsClosingIsAllowed) {
				waitForSync.wait();
			}
		}
		return wrappedProcess.waitFor();
	}
	
	public void allowStreamsClosing() {
		synchronized (waitForSync) {
			streamsClosingIsAllowed = true;
			waitForSync.notifyAll();
		}
	}

}
package nl.blackmantha.sandbox;

import java.lang.Thread.UncaughtExceptionHandler;

import nl.blackmantha.sandbox.pub.IApplet;

/**
 * 
 * Extends ISandboxConnector, so that the Sandbox class can call functions that can't be called from inside the sandbox.
 * 
 * @author github.com/Black-Mantha
 *
 */
public interface ISandboxConnector extends nl.blackmantha.sandbox.pub.ISandboxConnector, UncaughtExceptionHandler {
	void notifyConstructed(IApplet app);
	void appletCompleted();
}

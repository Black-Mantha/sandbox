package nl.blackmantha.sandbox;

import java.lang.Thread.UncaughtExceptionHandler;

import nl.blackmantha.sandbox.pub.IApplet;

/**
 * 
 * Interface AppletThread, so that the Thread object can call functions that pub.IAppletConnector can't. 
 * 
 * @author github.com/Black-Mantha
 *
 */
public interface ISandboxConnector extends nl.blackmantha.sandbox.pub.ISandboxConnector, UncaughtExceptionHandler {
	void notifyConstructed(IApplet app);
	void appletCompleted();
}

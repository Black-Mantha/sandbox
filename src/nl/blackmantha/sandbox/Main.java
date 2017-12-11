package nl.blackmantha.sandbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;

import nl.blackmantha.sandbox.Sandbox.SandboxThreadGroup;
import nl.blackmantha.sandbox.pub.IApplet;

/**
 * 
 * A class for testing Applets. Runs the Applet in a Sandbox, with a dummy   
 * 
 * @author github.com/Black-Mantha
 *
 */
public class Main {
	
	private static final String APPLET_INI_ERROR = "To make this applet runnable, make an applet.ini file as a resource in the nl.blackmantha.sandbox.pub folder, and put the following line in it: \"Main-Class: \", followed by the fully qualified class name of a class extending nl.blackmantha.sandbox.pub.Applet.";

	public static void main(String[] args) {
		runApplet();
	}
	
	public static void runApplet() { 
		// Read the ini
		InputStream in = Main.class.getResourceAsStream("pub/applet.ini");
		if (in==null) {
			System.err.println("Missing applet.ini\n");
			System.err.println(APPLET_INI_ERROR);
			System.exit(1);
		}
		String mainClassName = null;
		try {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line= reader.readLine()) != null) {
					if (line.startsWith("Main-Class: ")) {
						mainClassName = line.substring(12).trim();
						break;
					}
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mainClassName==null) {
			System.err.println("Missing Main-Class in applet.ini\n");
			System.err.println(APPLET_INI_ERROR);
			System.exit(1);
		}
		
		// Activate the security manager
		System.setSecurityManager(SwitchableSecurityManager.getInstance());
		
		// Prepare the Sandbox
		final Sandbox thread = new Sandbox(mainClassName, new ResourceBytecodeSource(), new ISandboxConnector() {
			@Override
			public void uncaughtException(Thread exThread, Throwable th) {
				if (SwitchableSecurityManager.isRestricted.get().booleanValue()) {
					System.err.println("Applet threw exception:");
				} else {
					System.err.println("Our code threw exception:");
				}
				th.printStackTrace();
			}
			
			@Override
			public void notifyConstructed(IApplet app) {
				System.out.println("notifyConstructed");
			}
			
			@Override
			public void appletCompleted() {
				System.out.println("appletCompleted");
			}
		});
		
		SandboxThreadGroup group = thread.getSandboxThreadGroup();
		thread.start();

		try {
			do {
				Object waiter = new Object();
				synchronized(waiter)  {waiter.wait(100);}
			} while (!thread.hasStarted);
			boolean success = group.join(0);
			if (!success) {
				System.err.println("Couldn't join");
				success = group.forceStop();
				if (!success)  System.exit(1);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

package nl.blackmantha.sandbox;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nl.blackmantha.sandbox.pub.IApplet;

public class Sandbox extends Thread {
	// Members to specify applet and callbacks
	protected String appClassName;
	protected ISandboxBytecodeSource codeSource;
	protected ISandboxConnector connector = null;
	// Member for security checks
	protected boolean hasStarted = false; // to protect against Applets calling Thread.getCurrentThread().run()

	// The ClassLoader shared by Applets, controlling what library classes are available to it 
	static final CentralSandboxClassLoader pubClassLoader = new CentralSandboxClassLoader();
	
	public Sandbox(String appClassName, ISandboxBytecodeSource codeSource) {
		super(new SandboxThreadGroup("ThreadGroup "+appClassName), "Applet "+appClassName);
		this.appClassName = appClassName;
		this.codeSource = codeSource;
	}

	public Sandbox(String appClassName, ISandboxBytecodeSource codeSource, ISandboxConnector connector) {
		this(appClassName, codeSource);
		setConnector(connector);
	}
	
	public void setConnector(ISandboxConnector connector) {
		this.connector = connector;
	}
	
	public SandboxThreadGroup getSandboxThreadGroup() {
		return (SandboxThreadGroup) super.getThreadGroup();
	}

	@Override
	public void run() {
		if (Thread.currentThread() != this)  throw new IllegalStateException("Do not call .run() directly; start the thread with .start()");
		if (hasStarted)  throw new SecurityException("Did you really just call Thread.getCurrentThread().run()?");
		hasStarted = true;
		if (connector==null)  throw new NullPointerException("No connector");

		SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
		((SandboxThreadGroup)getThreadGroup()).setMainThread(this);
		Thread.setDefaultUncaughtExceptionHandler(connector);
		
		// Load the applet class with its own classLoader
		ClassLoader appClassLoader = new SandboxClassLoader(pubClassLoader, codeSource);
		
		IApplet app;
		Class<?> appClass;
		Constructor<?> appConstr;
		try {
			appClass  = (Class<?>) appClassLoader.loadClass(appClassName); // It doesn't actually need to be an Applet, implementing IApplet and the <init>(ISandboxConnector) constructor is enough 
			appConstr = appClass.getConstructor(nl.blackmantha.sandbox.pub.ISandboxConnector.class);
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException | ClassNotFoundException e ) {
			connector.uncaughtException(this, e);
			return;
		}
			
		try {
			// initialize the Applet
			SwitchableSecurityManager.isRestricted.set(Boolean.TRUE);
			app = (IApplet) appConstr.newInstance(connector);
			SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
			appConstr = null;
			connector.notifyConstructed(app);
		
			// Start the Applet
			SwitchableSecurityManager.isRestricted.set(Boolean.TRUE);
			app.init();
			SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);

		} catch (IllegalAccessException | IllegalArgumentException | SecurityException | InstantiationException e ) {
			connector.uncaughtException(this, e);
			return;
		} catch (InvocationTargetException e) {
			connector.uncaughtException(this, e.getCause());
			return;
		}

		// TODO: start the message loop, for applets implementing one

		// Applet closes normally
		connector.appletCompleted();
	}
	
	/**
	 * Find the Sandbox this thread belongs to, or null if not Sandboxed.
	 * 
	 * @return boolean
	 */
	public static Sandbox getSandbox(Thread thread) {
		if (thread instanceof Sandbox) {
			return (Sandbox)thread;
		}
		ThreadGroup group = thread.getThreadGroup();
		while (group != null) {
			if (group instanceof Sandbox.SandboxThreadGroup) {
				return ((Sandbox.SandboxThreadGroup)group).getMainThread();
			}
			group = group.getParent();
		}
		return null;
	}

	/**
	 * 
	 * A ThreadGroup every sandbox thread must be a member of, directly or indirectly. This is used to identify threads
	 * with restricted permissions.
	 * 
	 * @author github.com/Black-Mantha
	 *
	 */
	public static class SandboxThreadGroup extends ThreadGroup {
		
		public SandboxThreadGroup(String name)  {super(name);}

		public SandboxThreadGroup(ThreadGroup parentGroup, String name)  {super(parentGroup, name);}

		private Sandbox mainThread = null;
		public Sandbox getMainThread()  {return mainThread;}
		void setMainThread(Sandbox mainThread)  {this.mainThread = mainThread;}

		@SuppressWarnings("deprecation")
		/**
		 * Try to stop all threads in this group, first by Thread.interrupt(), then by Thread.stop(). 
		 * @return whether we succeeded
		 * @throws InterruptedException
		 */
		boolean forceStop() throws InterruptedException {
			if (getSandbox(Thread.currentThread())==mainThread) {
				throw new IllegalAccessError("Can't forcestop a Sandbox from inside");
			}
			this.interrupt();
			if (this.join(3000))  return true;
			System.err.println(getName()+" refused to be interrupted");
			this.stop();
			if (this.join(1000))  return true;
			System.err.println(getName()+" refused to be stopped");
			return false;
		}
		
		/**
		 * Halt execution until all non-daemon threads of this group have stopped.
		 * @param timeout  How long to wait in milliseconds. 0 means to wait forever.
		 * @return whether we succeeded
		 * @throws InterruptedException
		 */
		boolean join(final int timeout) throws InterruptedException {
			Thread[] toJoinList = new Thread[8];
			int currTimeout = timeout;
			long timeStart = System.currentTimeMillis();
			while (true) {
				int nThread = this.enumerate(toJoinList);
				for (int iThread= 0; iThread<nThread; iThread++) {
					Thread toJoin = toJoinList[iThread];
					if (toJoin.isDaemon())  continue;
					toJoin.join(currTimeout);
					if (toJoin.isAlive())  return false;
					if (timeout!=0) {
						currTimeout = timeout - (int)(System.currentTimeMillis()-timeStart);
						if (currTimeout<=0)  return false;
					}
				}
				if (nThread!=toJoinList.length)  return true;
				// Grow the list since we need to make space
				toJoinList = new Thread[toJoinList.length * 2];
			}
		}
	}
}

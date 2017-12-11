package nl.blackmantha.sandbox;

import java.lang.ref.WeakReference;
import java.security.Permission;

public class SwitchableSecurityManager extends SandboxSecurityManager {

	private static WeakReference<SwitchableSecurityManager> instance = new WeakReference<SwitchableSecurityManager>(null);

	private SwitchableSecurityManager() {  }
	public synchronized static SecurityManager getInstance() {
		SwitchableSecurityManager result;
		if (instance==null || (result= instance.get())==null) {
			result = new SwitchableSecurityManager();
			instance = new WeakReference<SwitchableSecurityManager>(result);
			// Pre-load the Sandbox class before this SecurityManager is activated 
			try  {
				Class.forName("nl.blackmantha.sandbox.Sandbox");
			} catch (ClassNotFoundException e)  {throw new RuntimeException(e);}
		}
		return result;
	}
	
	/**
	 *  This static member is used to check if threads should be restricted. initialValue() supplies the default, but it can
	 *  be changed by classes which have access to this member. Note that both reflection and simply defining a class in this
	 *  package allow access, so it's not secure by itself. Suggested usage:
	 *  
	 *    Boolean wasRestricted = SwitchableSecurityManager.isRestricted.get();
	 *    SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
	 *    try {
	 *      < privileged code >
	 *    } finally {
	 *      SwitchableSecurityManager.isRestricted.set(wasRestricted);
	 *    }
	 */
	static ThreadLocal<Boolean> isRestricted = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			Thread thread = Thread.currentThread();
			boolean isInGroup = Sandbox.getSandbox(thread)!=null;
			return Boolean.valueOf(isInGroup);
		}
	};
	
	private final Object reentryCheck = new Object();
	@Override
	public void checkPermission(Permission perm) {
		// reentryCheck is needed because otherwise, isRestricted.get() could get into an infinite loop via ThreadLocal.initialValue() and this method
		if (Thread.holdsLock(reentryCheck))  return;
		Boolean bool;
		synchronized (reentryCheck) { 
			bool = isRestricted.get();
		}
		if (bool.booleanValue()) {
			super.checkPermission(perm);
		}
	}
}

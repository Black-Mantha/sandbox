package nl.blackmantha.sandbox;

import java.security.Permission;

public class SandboxSecurityManager extends SecurityManager {
	public SandboxSecurityManager() {  }
	
	@Override
	public void checkPermission(Permission perm) {
		throw new SecurityException("Permission "+perm+" not granted to applet");
	}
}

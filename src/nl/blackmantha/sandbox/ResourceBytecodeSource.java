package nl.blackmantha.sandbox;

import java.io.IOException;
import java.io.InputStream;

import nl.blackmantha.sandbox.pub.Util;

/**
 * An ISandboxBytecodeSource that gets its code by reading class files as resources. Only for debugging Applets.
 * 
 * @author github.com/Black-Mantha
 */
public class ResourceBytecodeSource implements ISandboxBytecodeSource {
	
	public ResourceBytecodeSource()  { }

	@Override
	public byte[] getByteCode(String name) {
		name = "/"+name.replace('.', '/')+".class";
		Boolean prevPermission = SwitchableSecurityManager.isRestricted.get();
		SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
		try {
			InputStream in = ResourceBytecodeSource.class.getResourceAsStream(name);
			if (in==null)  return null;
			try {
				try {
					return Util.readInputStream(in, 4092);
				} finally {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			SwitchableSecurityManager.isRestricted.set(prevPermission);
		}
		return null;
	}
}

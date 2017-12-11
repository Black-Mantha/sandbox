package nl.blackmantha.sandbox;

import java.io.IOException;
import java.io.InputStream;

import nl.blackmantha.sandbox.pub.Util;

/**
 * 
 * Controls which classes are available for applets. Those in the .pub package it loads itself, for some it defers to the default loader, and all others are unavailable. 
 * 
 * @author github.com/Black-Mantha
 *
 */
public class CentralSandboxClassLoader extends ClassLoader {
	public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> result = findLoadedClass(name);
		if (result==null) {
//			System.out.println("Public class load request: "+name);
			if (result==null) {
				for(WhiteListItem item : whiteListClass) {
					if (item.matches(name)) {
						if (item.loadHere) {
							byte[] byteCode;
							try {
								byteCode = getByteCode(name);
							} catch (IOException e) {
								throw new ClassNotFoundException("IOException while loading "+name, e);
							}
							if (byteCode!=null) {
								result = defineClass(name, byteCode, 0, byteCode.length);
							}
						} else {
							result = super.loadClass(name, resolve);
						}
						break;
					}
				}
			}
		}
		if (result==null) {
			throw new ClassNotFoundException(name+" not found or not available to applets");
		}
		if (resolve) {
			resolveClass(result);
		}
		return result;
	}
	
	private byte[] getByteCode(String name) throws IOException {
		String filename = "/"+name.replace('.', '/')+".class";
		InputStream in = SandboxClassLoader.class.getResourceAsStream(filename);
		if (in==null)  return null;
		try {
			return Util.readInputStream(in, 4096);
		} finally {
			in.close();
		}
	}
	
	private static final WhiteListItem[] whiteListClass = new WhiteListItem[] {
			// These classes are whitelisted to be loaded by the default ClassLoader
			new WhiteListItem("java.lang.", false), 
			// these are loaded with the default classLoader, so that they can be implemented outside the sandbox, but the interface is resolvable inside
			new WhiteListItem("nl.blackmantha.sandbox.pub.ISandboxConnector", false),
			new WhiteListItem("nl.blackmantha.sandbox.pub.IApplet", false),
			// these classes are available to the sandbox, but are limited to safe classes
			new WhiteListItem("nl.blackmantha.sandbox.pub.", true),
	};

	private static class WhiteListItem {
		String pattern;
		boolean loadHere;
		
		WhiteListItem(String pattern, boolean loadHere) {
			this.pattern= pattern;
			this.loadHere= loadHere;
		}
		
		boolean matches(String name) {
			return name.startsWith(pattern) && name.indexOf('.', pattern.length())<0;
		}
	}
}
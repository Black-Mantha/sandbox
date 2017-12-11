package nl.blackmantha.sandbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import nl.blackmantha.sandbox.pub.Util;

public class JarBytecodeSource extends JarFile implements ISandboxBytecodeSource {

	public JarBytecodeSource(File jar) throws IOException {
		super(jar);
	}

	@Override
	public byte[] getByteCode(String name) {
		Boolean wasRestricted = SwitchableSecurityManager.isRestricted.get();
		SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
		try {
			JarEntry entry = getJarEntry(name.replace('.',  '/')+".class");
			if (entry==null || entry.isDirectory())  throw new RuntimeException(name+" is not a class");
			
			InputStream in;
			try {
				in = getInputStream(entry);
				try {
					int fLength = (int) entry.getSize();
					if (fLength<0)  return Util.readInputStream(in, 4096);
					
					byte[] byteCode = new byte[fLength];
					int loc = 0;
					while (fLength>0) {
						int read = in.read(byteCode, loc, fLength);
						if (read<=0) {
							System.err.println("file length mismatch");
							return null;
						}
						loc+= read;
						fLength-= read;
					}
					return byteCode;
				} finally {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} finally {
			SwitchableSecurityManager.isRestricted.set(wasRestricted);
		}
	}
}

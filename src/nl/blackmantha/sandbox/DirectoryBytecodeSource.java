package nl.blackmantha.sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * An ISandboxBytecodeSource that gets its code by reading class files in a directory tree. Only for debugging Applets.
 * 
 * @author github.com/Black-Mantha
 */
public class DirectoryBytecodeSource implements ISandboxBytecodeSource {
	protected File rootDir;
	public DirectoryBytecodeSource(File rootDir) throws FileNotFoundException {
		if (!rootDir.isDirectory())  throw new FileNotFoundException(rootDir.getAbsolutePath()+" is not a directory");
		this.rootDir = rootDir;
	}

	@Override
	public byte[] getByteCode(String name) {
		final Boolean oldSecurity = SwitchableSecurityManager.isRestricted.get();
		SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
		try {
			File file = new File(rootDir, name.replace('.', '\\')+".class");
			FileInputStream fin;
			try {
				fin = new FileInputStream(file);
				try {
					int fLength = (int) file.length();
					byte[] byteCode = new byte[fLength];
					int loc = 0;
					while (fLength>0) {
						int read = fin.read(byteCode, loc, fLength);
						if (read<=0)  break;
						fLength-= read;
					}
					if (fLength!=0) {
						System.err.println("file length mismatch");
						return null;
					}
					return byteCode;
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						fin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				System.err.println(file.getAbsolutePath()+" doesn't exist");
			}
			return null;
		} finally {
			SwitchableSecurityManager.isRestricted.set(oldSecurity);
		}
	}
}

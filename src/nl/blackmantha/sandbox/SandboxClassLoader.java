package nl.blackmantha.sandbox;

import java.text.ParseException;

/**
 * 
 * Loads classes from the Applet. Gets the bytecode from a IAppletBytecodeSource.
 * 
 * It must have a nl.blackmantha.sandbox.CentralSandboxClassLoader as its parent.
 * 
 * While there should only be a single CentralSandboxClassLoader, every Applet should have
 * a SandboxClassLoader for it. That way, when all Objects of that Applet and this classloader 
 * become GC-able, the classes themselves can be unloaded.
 * 
 * @author github.com/Black-Mantha
 *
 */
public class SandboxClassLoader extends ClassLoader {
	
	final private ISandboxBytecodeSource connector;

	public SandboxClassLoader(ClassLoader parent, ISandboxBytecodeSource connector) {
		super(parent);
		if (!parent.getClass().getName().equals("nl.blackmantha.sandbox.CentralSandboxClassLoader")) {
			throw new RuntimeException("SandboxClassLoader must have a CentralSandboxClassLoader as parent");
		}
		this.connector = connector;
	}

	/**
	 * Make a class in the sandbox, after checking for forbidden code constructs
	 */
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] bytecode = connector.getByteCode(name);
		if (bytecode!=null) {
			ByteCodeAnalyser analysis;
			try {
				analysis = new ByteCodeAnalyser(bytecode);
			} catch (ParseException e) {
				throw new ClassNotFoundException("While loading "+name, e);
			}
			if (analysis.hasNatives())  throw new SecurityException("native functions not allowed");
			if (analysis.hasMethodDeclared("finalize", "()"))  throw new SecurityException("finalizers not allowed");
			return defineClass(name, bytecode, 0, bytecode.length);
		}
		throw new ClassNotFoundException(name);
	}
}

package nl.blackmantha.sandbox;

public interface ISandboxBytecodeSource {
	byte[] getByteCode(String name); // For getting the bytecode of a class inside the plugin
}

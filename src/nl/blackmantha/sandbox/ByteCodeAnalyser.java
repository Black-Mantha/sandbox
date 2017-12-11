package nl.blackmantha.sandbox;

import java.text.ParseException;

public class ByteCodeAnalyser {
	final byte[] bytecode;
	final int[] poolIndex;
	final int nMethods;
	final int[] methodIndex;
	final int endPool;
	
	final int access_flags;
	
	public ByteCodeAnalyser(byte[] bytecode) throws ParseException {
		this.bytecode = bytecode;
		if (bytecode.length<24 || bytecode[0]!=0xCA-256 || bytecode[1]!=0xFE-256 || bytecode[2]!=0xBA-256 || bytecode[3]!=0xBE-256) {
			throw new ParseException("Not a class file", 0);
		}
		// Index constant pool
		int poolsize = read16(8);
		poolIndex = new int[poolsize]; 
		int at = 10;
		for (int idx = 1; idx<poolsize; idx++) {
			poolIndex[idx] = at;
			switch (bytecode[at]) {
			case 1: 
				int length = read16(at+1);
//				System.out.println("idx "+idx+":"+new String(bytecode, at+3, length));
				at += 3 + length; break;
			case 7: case 8: case 16: 
				at += 3; break;
			case 3: case 4: case 9: case 10: case 11: case 12: case 18: 
				at += 5; break;
			case 5: case 6:
				at += 9; idx++; break;
			case 15:
				at += 4; break;
			default:
				throw new ParseException("Unknown constant pool tag "+bytecode[at], at);
			}
		}
		endPool = at;
		access_flags = read16(at);
//		System.out.println("Verifying class "+readString(read16(poolIndex[read16(endPool+2)]+1)));
		// Skip interfaces
		at += 6;
		int nInterface = read16(at);
		at += 2 + nInterface*2;
		// Skip fields
		int nField = read16(at);
		at+=2;
		for (int i=0; i<nField; i++) {
			int nAttibute = read16(at+6); 
			at += 8 + nAttibute*7;
		}
		// Loop over methods
		nMethods = read16(at);
		methodIndex = new int[nMethods];
		at += 2;
		for (int i=0; i<nMethods; i++) {
//			System.out.println("Method: "+readString(read16(at+2)));
			methodIndex[i]= at;
			int nAttribute = read16(at+6);
			at+= 8;
			for (int j=0; j<nAttribute; j++) {
				int length = read32(at+2);
				at += 6+length;
			}
		}
	}

	int read16(int at) {
		return 256*(bytecode[at]&0xFF) + (bytecode[at+1]&0xFF);
	}

	int read32(int at) {
		return 16777216*(bytecode[at]&0xFF) + 65536*(bytecode[at+1]&0xFF) + 256*(bytecode[at+2]&0xFF) + (bytecode[at+3]&0xFF);
	}
	
	String readString(int index) {
		int at = poolIndex[index];
		final int length = read16(at+1);
		return new String(bytecode, at+3, length);
	}
	
	boolean isInterface() {
		return (access_flags & 0x0200) != 0; 
	}
	
	boolean isAbstract() {
		return (access_flags & 0x0400) != 0; 
	}
	
	boolean hasMethodDeclared(String name, String descriptor) {
		for (int i=0; i<nMethods; i++) {
			if (readString(read16(methodIndex[i]+2)).equals(name) && 
				readString(read16(methodIndex[i]+4)).startsWith(descriptor))  return true;
		}
		return false;
	}
	
	boolean hasNatives() {
		for (int i=0; i<nMethods; i++) {
			if ((read16(methodIndex[i]) & 0x0100)!=0)  return true;
		}
		return false;
	}
}

package com.shk.js.type;

public class Convert {
	protected static final char[] HEX_LOWERCASE = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f',
	};
	
	protected static final char[] HEX_UPPERCASE = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F',
	};
	
	public static String toHexString(byte[] input) {
		return toHexString(input, 0, input.length);
	}
	
	public static String toHexString(byte[] input, int offset, int len) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = offset; i < len; i++) {
			byte b = input[i];
			
			sb.append(HEX_LOWERCASE[(b >> 4) & 0xf]);
			sb.append(HEX_LOWERCASE[b & 0xf]);
		}
		
		return sb.toString();
	}
}

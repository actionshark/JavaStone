package com.shk.js.data;

public class Convert {
	protected static final char[] HEX_CHAR = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f', };

	public static String toHexString(byte[] input) {
		return toHexString(input, 0, input.length);
	}

	public static String toHexString(byte[] input, int offset, int length) {
		StringBuilder sb = new StringBuilder();

		for (int i = offset; i < length; i++) {
			byte b = input[i];

			sb.append(HEX_CHAR[(b >> 4) & 0xf]);
			sb.append(HEX_CHAR[b & 0xf]);
		}

		return sb.toString();
	}
}

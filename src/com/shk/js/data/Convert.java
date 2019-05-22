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
	
	public static long bs2n(byte[] bytes, int offset, int length) {
		int n = 0;
		
		for (int i = 0; i < length; i++) {
			n = n << 8 | (bytes[offset + i] & 0xff);
		}
		
		return n;
	}
	
	public static long bs2n(byte[] bytes) {
		return bs2n(bytes, 0, bytes.length);
	}
	
	public static void n2bs(long n, byte[] bytes, int offset, int length) {
		for (int i = length - 1; i >= 0; i--) {
			bytes[offset + i] = (byte) (n & 0xff);
			n >>>= 8;
		}
	}
	
	public static void n2bs(long n, byte[] bytes) {
		n2bs(n, bytes, 0, bytes.length);
	}
	
	public static byte[] n2bs(long n, int length) {
		byte[] bs = new byte[length];
		n2bs(n, bs);
		return bs;
	}
}

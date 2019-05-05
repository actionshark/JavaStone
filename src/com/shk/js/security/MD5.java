package com.shk.js.security;

import java.io.InputStream;
import java.security.MessageDigest;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.type.Convert;

public class MD5 {
	public static String encode(byte[] input) {
		return encode(input, 0, input.length);
	}

	public static String encode(byte[] input, int offset, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();

			md.update(input, offset, len);

			byte[] output = md.digest();
			return Convert.toHexString(output);
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}

		return null;
	}

	public static String encode(InputStream is) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();

			byte[] buf = new byte[1024 * 16];
			int len = -1;
			while ((len = is.read(buf)) > 0) {
				md.update(buf, 0, len);
			}

			byte[] output = md.digest();
			return Convert.toHexString(output);
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}

		return null;
	}
}

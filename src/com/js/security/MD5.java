package com.js.security;

import java.io.InputStream;
import java.security.MessageDigest;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.type.Convert;

public class MD5 {
	public static final String TAG = MD5.class.getSimpleName();
	
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
			Logger.getInstance().print(TAG, Level.E, e);
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
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		return null;
	}
}

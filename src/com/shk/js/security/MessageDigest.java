package com.shk.js.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.type.Convert;

public class MessageDigest {
	private java.security.MessageDigest mMD;

	public MessageDigest(String name) {
		if (name == null) {
			name = "MD5";
		}

		try {
			mMD = java.security.MessageDigest.getInstance(name);
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}
	}
	
	public void reset() {
		mMD.reset();
	}

	public void update(byte b) {
		mMD.update(b);
	}

	public void update(byte[] bs, int offset, int length) {
		mMD.update(bs, offset, length);
	}

	public void update(byte[] bs) {
		mMD.update(bs);
	}

	public void update(String str) {
		mMD.update(str.getBytes());
	}

	public void update(File file) {
		try {
			InputStream is = new FileInputStream(file);
			update(is);
			is.close();
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}
	}

	public void update(InputStream is) {
		byte[] bs = new byte[1024 * 1024];
		int len;
		
		try {
			while ((len = is.read(bs)) > 0) {
				mMD.update(bs, 0, len);
			}
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}
	}
	
	public byte[] digestBytes() {
		return mMD.digest();
	}
	
	public String digestHex() {
		byte[] bs = mMD.digest();
		return Convert.toHexString(bs);
	}
}

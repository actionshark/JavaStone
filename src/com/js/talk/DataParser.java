package com.js.talk;

import com.js.log.Level;
import com.js.log.Logger;

import net.sf.json.JSONObject;

public class DataParser {
	public static final String TAG = DataParser.class.getSimpleName();
	
	public static interface IOnParseListener {
		public void onParse(JSONObject jo);
	}
	
	public static final int LENGTH_SIZE = 2;
	
	private byte[] mData;
	private int mOffset = 0;
	private int mLength = 0;
	
	public void clear() {
		mOffset = 0;
		mLength = 0;
	}
	
	public boolean parse(byte[] data, int offset, int length, IOnParseListener listener) {
		if (mLength == 0) {
			mData = data;
			mOffset = offset;
			mLength = length;
		} else {
			int len = mLength + length;
			byte[] d;
			
			if (mData.length >= len) {
				d = mData;
			} else {
				d = new byte[len];
			}
			
			for (int i = 0; i < mLength; i++) {
				d[i] = mData[mOffset + i];
			}
			
			for (int i = 0; i < length; i++) {
				d[mLength + i] = data[offset + i];
			}
			
			mData = d;
			mOffset = 0;
			mLength = len;
		}
		
		while (mLength > LENGTH_SIZE) {
			int len = 0;
			for (int i = 0; i < LENGTH_SIZE; i++) {
				len = len * 256 + (mData[mOffset + i] & 0xff);
			}
			
			if (mLength < LENGTH_SIZE + len) {
				break;
			}
			
			try {
				String str = new String(data, offset, length);
				JSONObject jo = JSONObject.fromObject(str);
				listener.onParse(jo);
			} catch (Exception e) {
				Logger.getInstance().print(TAG, Level.E, e);
				
				return false;
			}
			
			mOffset += LENGTH_SIZE + len;
			mLength -= LENGTH_SIZE + len;
		}
		
		return true;
	}
}

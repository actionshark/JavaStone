package com.shk.js.talk;

public class DataCoder {
	public static final String TAG = DataCoder.class.getSimpleName();
	
	public static interface IOnDecodeListener {
		public void onDecode(byte[] data, int offset, int length);
	}
	
	public static final int LENGTH_SIZE = 2;
	
	private byte[] mData;
	private int mOffset = 0;
	private int mLength = 0;
	
	public static byte[] encode(byte[] data, int offset, int length) {
		byte[] bs = new byte[LENGTH_SIZE + length];
		
		int len = length;
		for (int i = 0; i < LENGTH_SIZE; i++) {
			bs[LENGTH_SIZE - i - 1] = (byte) (len & 0xff);
			len >>= 8;
		}
		
		for (int i = 0; i < length; i++) {
			bs[LENGTH_SIZE + i] = data[offset + i];
		}
		
		return bs;
	}
	
	public void clear() {
		mOffset = 0;
		mLength = 0;
	}
	
	public void decode(byte[] data, int offset, int length, IOnDecodeListener listener) {
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
			
			listener.onDecode(mData, mOffset + LENGTH_SIZE, len);
			
			mOffset += LENGTH_SIZE + len;
			mLength -= LENGTH_SIZE + len;
		}
	}
}

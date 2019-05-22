package com.shk.js.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.thread.ThreadUtil;

public class StreamReader {
	private static final int BUF_SIZE = 1024 * 1024;

	private InputStream mInputStream;

	public StreamReader(String filepath) {
		try {
			mInputStream = new FileInputStream(filepath);
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}
	}

	public StreamReader(File file) {
		try {
			mInputStream = new FileInputStream(file);
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}
	}

	public StreamReader(InputStream is) {
		mInputStream = is;
	}

	public byte[] readBytes() {
		return readBytes(Integer.MAX_VALUE);
	}

	public byte[] readBytes(int max) {
		List<byte[]> list = new ArrayList<byte[]>();
		int offset = 0;

		try {
			byte[] bs = new byte[BUF_SIZE];

			while (true) {
				int m = Math.min(bs.length - offset, max);
				int len = mInputStream.read(bs, offset, m);
				if (len <= 0) {
					break;
				}

				offset += len;
				if (offset >= bs.length) {
					list.add(bs);
					bs = new byte[BUF_SIZE];
					offset = 0;
				}

				max -= len;
				if (max <= 0) {
					break;
				}
			}

			list.add(bs);
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}

		byte[] bytes = new byte[BUF_SIZE * (list.size() - 1) + offset];
		int os = 0;

		for (int i = 0; i < list.size() - 1; i++) {
			byte[] bs = list.get(i);

			for (int j = 0; j < bs.length; j++) {
				bytes[os++] = bs[j];
			}
		}

		if (offset > 0) {
			byte[] bs = list.get(list.size() - 1);

			for (int i = 0; i < offset; i++) {
				bytes[os++] = bs[i];
			}
		}

		return bytes;
	}
	
	public byte[] readFull(int size) {
		byte[] result = new byte[size];
		readFull(result);
		return result;
	}
	
	public void readFull(byte[] bs) {
		try {
			int offset = 0;
			
			while (offset < bs.length) {
				int len = mInputStream.read(bs, offset, bs.length - offset);
				
				if (len <= 0) {
					ThreadUtil.sleep(100);
				}
				
				offset += len;
			}
		} catch(Exception e) {
			Logger.print(Level.E, e);
		}
	}

	public String readString() {
		return new String(readBytes());
	}

	public void close() {
		try {
			mInputStream.close();
		} catch (Exception e) {
			Logger.print(Level.E, e);
		}
	}
}

package com.js.app.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	public static void copy(InputStream is, OutputStream os) throws Exception {
		byte[] buf = new byte[1024 * 1024];
		int len = -1;
		
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
	}
	
	public static void delete(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (File child : children) {
				delete(child);
			}
		}
		
		dir.delete();
	}
	
	public static void clearDir(File dir) {
		File[] children = dir.listFiles();
		for (File child : children) {
			delete(child);
		}
	}
}

package com.js.app.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.js.log.Level;
import com.js.log.Logger;

public class FileUtil {
	public static final String TAG = FileUtil.class.getSimpleName();
	
	public static boolean copy(InputStream is, OutputStream os) {
		try {
			byte[] buf = new byte[1024 * 1024];
			int len = -1;
			
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		return false;
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

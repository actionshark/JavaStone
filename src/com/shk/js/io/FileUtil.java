package com.shk.js.io;

import java.io.File;

public class FileUtil {
	public static void delete(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child : children) {
				delete(child);
			}
		}

		file.delete();
	}

	public static void clearDir(File dir) {
		File[] children = dir.listFiles();
		for (File child : children) {
			delete(child);
		}
	}
}

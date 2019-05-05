package com.shk.js.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileLogger extends Logger {
	protected final List<File> mFileList = new ArrayList<File>();
	protected int mCurFileIndex = 0;
	
	protected long mLengthLimit = 1024 * 1024 * 16;
	
	public synchronized void setFiles(String... paths) {
		File[] files = new File[paths.length];
		
		for (int i = 0; i < files.length; i++) {
			files[i] = new File(paths[i]);
		}
		
		setFiles(files);
	}
	
	public synchronized void setFiles(File... files) {
		mFileList.clear();
		
		for (File file : files) {
			try {
				File parent = file.getParentFile();
				if (parent != null && parent.exists() == false) {
					parent.mkdirs();
				}
				
				if (file.exists() == false) {
					file.createNewFile();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			mFileList.add(file);
		}
	}
	
	public synchronized void setLengthLimit(long length) {
		mLengthLimit = length;
	}
	
	@Override
	protected synchronized void onPrint(String tag, Level level, String content) throws Exception {
		boolean append = true;
		
		if (mCurFileIndex >= mFileList.size()) {
			mCurFileIndex = 0;
			append = false;
		}
		
		do {
			File file = mFileList.get(mCurFileIndex);
			long length = file.length();
			
			if (length < mLengthLimit) {
				break;
			}
			
			append = false;
			
			if (++mCurFileIndex >= mFileList.size()) {
				mCurFileIndex = 0;
				break;
			}
		} while (true);
		
		File file = mFileList.get(mCurFileIndex);
		OutputStream os = new FileOutputStream(file, append);
		
		String string = String.format("%s %s %s", tag, level.getText(), content);
		byte[] buf = string.getBytes();
		os.write(buf);
		os.close();
	}
}

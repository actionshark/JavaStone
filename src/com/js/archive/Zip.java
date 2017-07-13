package com.js.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.thread.ThreadUtil;

public class Zip implements IArchive {
	public static final String TAG = Zip.class.getSimpleName();

	private IArchiveListener mListener;

	@Override
	public synchronized void setListener(IArchiveListener listener) {
		mListener = listener;
	}

	@Override
	public void compressSync(File file, OutputStream output) {
		// TODO
	}

	@Override
	public void compress(final File file, final OutputStream output) {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				compressSync(file, output);
			}
		});
	}

	@Override
	public void decompressSync(InputStream input, File dir) {
		try {
			ZipInputStream zis = new ZipInputStream(input);
			ZipEntry entry = null;
			byte[] buf = new byte[1024 * 16];

			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				File file = new File(dir, name);
				
				if (entry.isDirectory()) {
					if (file.exists() == false) {
						file.mkdirs();
					}
				} else {
					File parent = file.getParentFile();
					if (parent.exists() == false) {
						parent.mkdirs();
					}
					
					FileOutputStream output = new FileOutputStream(file);
					int len = -1;
					while ((len = zis.read(buf)) > 0) {
						output.write(buf, 0, len);
					}
					
					output.close();
				}
				
				notityProgress(name, file.getPath());
			}
			
			zis.close();
			
			notityFinish();
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);

			notityException(e);
		}
	}

	@Override
	public void decompress(final InputStream input, final File dir) {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				decompressSync(input, dir);
			}
		});
	}

	@Override
	public void stop() {
	}

	private void notityProgress(final String from, final String to) {
		synchronized (Zip.this) {
			if (mListener != null) {
				mListener.onProgress(Zip.this, from, to);
			}
		}
	}

	private void notityException(final Exception ex) {
		synchronized (Zip.this) {
			if (mListener != null) {
				mListener.onException(Zip.this, ex);
			}
		}
	}

	private void notityFinish() {
		synchronized (Zip.this) {
			if (mListener != null) {
				mListener.onFinish(Zip.this);
			}
		}
	}
}

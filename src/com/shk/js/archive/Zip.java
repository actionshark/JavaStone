package com.shk.js.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.thread.ThreadUtil;

public class Zip implements IArchive {
	private IArchiveListener mListener;

	@Override
	public synchronized void setListener(IArchiveListener listener) {
		mListener = listener;
	}

	@Override
	public void compress(File file, OutputStream output) {
		// TODO
	}

	@Override
	public void compressAsync(final File file, final OutputStream output) {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				compress(file, output);
			}
		});
	}

	@Override
	public void decompress(InputStream input, File dir) {
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
			Logger.print(Level.E, e);

			notityException(e);
		}
	}

	@Override
	public void decompressAsync(final InputStream input, final File dir) {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				decompress(input, dir);
			}
		});
	}

	@Override
	public void stop() {
		// TODO
	}

	private synchronized void notityProgress(String from, String to) {
		if (mListener != null) {
			mListener.onProgress(Zip.this, from, to);
		}
	}

	private synchronized void notityException(Exception ex) {
		if (mListener != null) {
			mListener.onException(Zip.this, ex);
		}
	}

	private synchronized void notityFinish() {
		if (mListener != null) {
			mListener.onFinish(Zip.this);
		}
	}
}

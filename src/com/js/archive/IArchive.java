package com.js.archive;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface IArchive {
	public void setListener(IArchiveListener listener);
	
	public void compress(File file, OutputStream output);
	public void compressAsync(File file, OutputStream output);
	
	public void decompress(InputStream input, File dir);
	public void decompressAsync(InputStream input, File dir);
	
	public void stop();
}

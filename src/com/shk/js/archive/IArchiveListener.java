package com.shk.js.archive;

public interface IArchiveListener {
	public void onProgress(IArchive archive, String from, String to);
	public void onException(IArchive archive, Exception ex);
	public void onFinish(IArchive archive);
}

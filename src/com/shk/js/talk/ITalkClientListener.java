package com.shk.js.talk;

public interface ITalkClientListener {
	public void onOffline(TalkClient client);
	public void onConnecting(TalkClient client);
	public void onConnected(TalkClient client);
	public void onReceived(TalkClient client,
		byte[] data, int offset, int length);
}

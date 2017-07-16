package com.js.talk;

public interface ITalkClientListener {
	public void onConnected(TalkClient client);
	public void onConnectFailed(TalkClient client);
	
	public void onReceived(TalkClient client,
		byte[] data, int offset, int length);
	
	public void onDisconnected(TalkClient client);
	public void onClosed(TalkClient client);
}

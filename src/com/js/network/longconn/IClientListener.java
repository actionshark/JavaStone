package com.js.network.longconn;

public interface IClientListener {
	public void onConnected(LongClient client);
	public void onConnectFailed(LongClient client);
	public void onDisconnected(LongClient client);
	public void onClosed(LongClient client);
	
	public void onSended(LongClient client, boolean success);
	public void onReceived(LongClient client, byte[] data, int offset, int length);
}

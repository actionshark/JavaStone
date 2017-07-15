package com.js.network;

public interface IClientListener {
	public void onConnected(NetClient client);
	public void onConnectFailed(NetClient client);
	public void onDisconnected(NetClient client);
	public void onClosed(NetClient client);
	
	public void onSended(NetClient client, boolean success);
	public void onReceived(NetClient client, byte[] data, int offset, int length);
}

package com.js.network;

public interface IClientListener {
	public void onOffline(NetClient client);
	public void onConnecting(NetClient client);
	public void onConnected(NetClient client);
	
	public void onSended(NetClient client, boolean success);
	public void onReceived(NetClient client, byte[] data, int offset, int length);
}

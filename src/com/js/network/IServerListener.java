package com.js.network;

public interface IServerListener {
	public void onConnected(NetServer server);
	public void onConnectFailed(NetServer server);
	public void onDisconnected(NetServer server);
	public void onClosed(NetServer server);
	
	public void onAccepted(NetServer server, NetClient client);
	public void onReceived(NetServer server, NetClient client, byte[] data, int offset, int length);
	public void onLeaved(NetServer server, NetClient client);
}

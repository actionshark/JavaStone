package com.shk.js.network;

public interface IServerListener {
	public void onOffline(NetServer server);

	public void onConnecting(NetServer server);

	public void onConnected(NetServer server);

	public void onAccepted(NetServer server, NetClient client);

	public void onReceived(NetServer server, NetClient client, byte[] data, int offset, int length);

	public void onLeaved(NetServer server, NetClient client);
}

package com.js.network.longconn;

import java.net.Socket;

public interface IServerListener {
	public void onConnected(LongServer server);
	public void onConnectFailed(LongServer server);
	public void onDisconnected(LongServer server);
	public void onClosed(LongServer server);
	
	public void onAccepted(LongServer server, Socket socket);
	public void onReceived(LongServer server, Socket socket, byte[] data, int offset, int length);
	public void onLeaved(LongServer server, Socket socket);
}

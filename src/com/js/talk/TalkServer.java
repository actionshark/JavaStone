package com.js.talk;

import java.util.HashMap;
import java.util.Map;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.network.IServerListener;
import com.js.network.NetClient;
import com.js.network.NetServer;
import com.js.talk.DataCoder.IOnDecodeListener;

public class TalkServer <T extends UserInfo> {
	public static final String TAG = TalkServer.class.getSimpleName();
	
	protected class ClientNode {
		public TalkClient client;
		public T userInfo;
		public final DataCoder parser = new DataCoder();
	}
	
	protected NetServer mServer;
	
	protected final Map<Integer, T> mUserInfos = new HashMap<Integer, T>();
	protected final Map<NetClient, ClientNode> mClientNodes = new HashMap<NetClient, ClientNode>();
	
	protected ITalkServerListener<T> mListener;
	
	public TalkServer() {
	}
	
	public synchronized void setServer(NetServer server) {
		mServer = server;
	}
	
	public synchronized void setListener(ITalkServerListener<T> listener) {
		mListener = listener;
	}
	
	public synchronized void addUser(T ui) {
		removeUser(ui.id);
		
		mUserInfos.put(ui.id, ui);
		
		ClientNode cn = mClientNodes.get(ui.client.getClient());
		cn.userInfo = ui;
	}
	
	public synchronized void removeUser(int id) {
		if (mUserInfos.containsKey(id)) {
			T ou = mUserInfos.remove(id);
			
			mClientNodes.remove(ou.client.getClient());
			ou.client.getClient().close(false);
		}
	}
	
	public synchronized T findUser(int id) {
		return mUserInfos.get(id);
	}
	
	public synchronized void start() {
		mServer.connectAsync();
		mServer.setListener(new IServerListener() {
			@Override
			public void onAccepted(NetServer server, NetClient client) {
				Logger.getInstance().print(TAG, Level.D);
				
				ClientNode cn = new ClientNode();
				
				cn.client = new TalkClient();
				cn.client.setClient(client);
				
				mClientNodes.put(client, cn);
			}
			
			@Override
			public void onReceived(NetServer server, final NetClient client,
					byte[] data, int offset, int length) {
				
				Logger.getInstance().print(TAG, Level.D);
				
				synchronized (TalkServer.this) {
					final ClientNode cn = mClientNodes.get(client);
					if (cn == null) {
						return;
					}
					
					cn.parser.decode(data, offset, length, new IOnDecodeListener() {
						@Override
						public void onDecode(byte[] data, int offset, int length) {
							if (mListener != null) {
								try {
									mListener.onReceived(TalkServer.this, cn.client,
										cn.userInfo, data, offset, length);
								} catch (Exception e) {
									Logger.getInstance().print(TAG, Level.E, e);
								}
							}
						}
					});
				}
			}
			
			@Override
			public void onLeaved(NetServer server, NetClient client) {
				Logger.getInstance().print(TAG, Level.D);
				
				synchronized (TalkServer.this) {
					if (mListener != null) {
						ClientNode cn = mClientNodes.get(client);
						
						if (cn != null) {
							try {
								mListener.onLeaved(TalkServer.this, cn.client, cn.userInfo);
							} catch (Exception e) {
								Logger.getInstance().print(TAG, Level.E, e);
							}
							
							if (cn.userInfo != null) {
								removeUser(cn.userInfo.id);
							}
						}
					}
				}
			}
			
			@Override
			public void onOffline(NetServer server) {
				Logger.getInstance().print(TAG, Level.D);
			}
			
			@Override
			public void onConnecting(NetServer server) {
				Logger.getInstance().print(TAG, Level.D);
			}
			
			@Override
			public void onConnected(NetServer server) {
				Logger.getInstance().print(TAG, Level.D);
			}
		});
	}
}

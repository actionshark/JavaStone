package com.js.talk;

import java.util.HashMap;
import java.util.Map;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.network.IServerListener;
import com.js.network.NetClient;
import com.js.network.NetServer;
import com.js.talk.DataParser.IOnParseListener;

import net.sf.json.JSONObject;

public class TalkServer <T extends UserInfo> {
	public static final String TAG = TalkServer.class.getSimpleName();
	
	protected class ClientNode {
		public T userInfo;
		public final DataParser parser = new DataParser();
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
		
		ClientNode cn = mClientNodes.get(ui.netClient);
		cn.userInfo = ui;
	}
	
	public synchronized void removeUser(int id) {
		if (mUserInfos.containsKey(id)) {
			T ou = mUserInfos.remove(id);
			
			mClientNodes.remove(ou.netClient);
			ou.netClient.close();
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
			}
			
			@Override
			public void onReceived(NetServer server, NetClient client,
					byte[] data, int offset, int length) {
				
				synchronized (TalkServer.this) {
					final ClientNode cn;
					if (mClientNodes.containsKey(client)) {
						cn = mClientNodes.get(client);
					} else {
						cn = new ClientNode();
						
						mClientNodes.put(client, cn);
					}
					
					boolean ret = cn.parser.parse(data, offset, length, new IOnParseListener() {
						@Override
						public void onParse(JSONObject jo) {
							if (mListener != null) {
								try {
									mListener.onReceived(TalkServer.this, cn.userInfo, jo);
								} catch (Exception e) {
									Logger.getInstance().print(TAG, Level.E, e);
								}
							}
						}
					});
					
					if (ret == false) {
						client.close();
					}
				}
			}
			
			@Override
			public void onLeaved(NetServer server, NetClient client) {
				synchronized (TalkServer.this) {
					if (mListener != null) {
						ClientNode cn = mClientNodes.get(client);
						
						if (cn != null) {
							try {
								mListener.onLeaved(TalkServer.this, cn.userInfo);
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
			public void onConnected(NetServer server) {
			}
			
			@Override
			public void onConnectFailed(NetServer server) {
			}
			
			@Override
			public void onDisconnected(NetServer server) {
			}
			
			@Override
			public void onClosed(NetServer server) {
			}
		});
	}
}

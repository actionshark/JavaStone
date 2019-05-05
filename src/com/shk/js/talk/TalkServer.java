package com.shk.js.talk;

import java.util.HashMap;
import java.util.Map;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.network.IServerListener;
import com.shk.js.network.NetClient;
import com.shk.js.network.NetServer;
import com.shk.js.talk.DataCoder.IOnDecodeListener;

public class TalkServer<T extends UserInfo> {
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
			ou.client.getClient().close();
		}
	}

	public synchronized T findUser(int id) {
		return mUserInfos.get(id);
	}

	public synchronized void start() {
		mServer.setListener(new IServerListener() {
			@Override
			public void onAccepted(NetServer server, NetClient client) {
				Logger.print(Level.D);

				ClientNode cn = new ClientNode();

				cn.client = new TalkClient();
				cn.client.setClient(client);

				synchronized (TalkServer.this) {
					mClientNodes.put(client, cn);
				}
			}

			@Override
			public void onReceived(NetServer server, final NetClient client, byte[] data, int offset, int length) {
				Logger.print(Level.D);

				ClientNode cn = null;
				synchronized (TalkServer.this) {
					cn = mClientNodes.get(client);
				}
				if (cn == null) {
					return;
				}
				final ClientNode CN = cn;

				synchronized (cn.parser) {
					cn.parser.decode(data, offset, length, new IOnDecodeListener() {
						@Override
						public void onDecode(byte[] data, int offset, int length) {
							ITalkServerListener<T> listener = mListener;
							if (listener == null) {
								return;
							}

							try {
								listener.onReceived(TalkServer.this, CN.client, CN.userInfo, data, offset, length);
							} catch (Exception e) {
								Logger.print(Level.E, e);
							}
						}
					});
				}
			}

			@Override
			public void onLeaved(NetServer server, NetClient client) {
				Logger.print(Level.D);

				ClientNode cn = null;
				synchronized (TalkServer.this) {
					cn = mClientNodes.get(client);
				}

				if (cn == null) {
					return;
				}

				if (cn.userInfo != null) {
					removeUser(cn.userInfo.id);
				}

				ITalkServerListener<T> listener = mListener;
				if (listener == null) {
					return;
				}

				try {
					listener.onLeaved(TalkServer.this, cn.client, cn.userInfo);
				} catch (Exception e) {
					Logger.print(Level.E, e);
				}
			}

			@Override
			public void onOffline(NetServer server) {
				Logger.print(Level.D);
			}

			@Override
			public void onConnecting(NetServer server) {
				Logger.print(Level.D);
			}

			@Override
			public void onConnected(NetServer server) {
				Logger.print(Level.D);
			}
		});

		mServer.connectAsync();
	}
}

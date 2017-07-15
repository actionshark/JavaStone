package com.js.talk;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.network.IClientListener;
import com.js.network.NetClient;
import com.js.talk.DataParser.IOnParseListener;

import net.sf.json.JSONObject;

public class TalkClient {
	public static final String TAG = TalkClient.class.getSimpleName();
	
	protected NetClient mClient;
	
	protected final DataParser mParser = new DataParser();
	
	protected ITalkClientListener mListener;
	
	public TalkClient() {
	}
	
	public synchronized void setClient(NetClient client) {
		mClient = client;
	}
	
	public synchronized void setListener(ITalkClientListener listener) {
		mListener = listener;
	}
	
	public synchronized void send(byte[] data, int offset, int length) {
		mClient.sendAsync(data, offset, length);
	}
	
	public synchronized void start() {
		mParser.clear();
		
		mClient.connectAsync();
		mClient.setListener(new IClientListener() {
			@Override
			public void onReceived(NetClient client, byte[] data, int offset, int length) {
				synchronized (TalkClient.this) {
					boolean ret = mParser.parse(data, offset, length, new IOnParseListener() {
						@Override
						public void onParse(JSONObject jo) {
							if (mListener != null) {
								try {
									mListener.onReceived(TalkClient.this, jo);
								} catch (Exception e) {
									Logger.getInstance().print(TAG, Level.E, e);
								}
							}
						}
					});
					
					if (ret == false) {
						mClient.close();
					}
				}
			}
			
			@Override
			public void onSended(NetClient client, boolean success) {
			}
			
			@Override
			public void onConnected(NetClient client) {
			}
			
			@Override
			public void onConnectFailed(NetClient client) {
			}
			
			@Override
			public void onDisconnected(NetClient client) {
			}
			
			@Override
			public void onClosed(NetClient client) {
			}
		});
	}
}

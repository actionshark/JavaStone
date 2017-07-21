package com.js.talk;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.network.IClientListener;
import com.js.network.NetClient;
import com.js.talk.DataCoder.IOnDecodeListener;

public class TalkClient {
	public static final String TAG = TalkClient.class.getSimpleName();
	
	protected NetClient mClient;
	
	protected final DataCoder mParser = new DataCoder();
	
	protected ITalkClientListener mListener;
	
	public TalkClient() {
	}
	
	public synchronized void setClient(NetClient client) {
		mClient = client;
	}
	
	public synchronized NetClient getClient() {
		return mClient;
	}
	
	public synchronized void setListener(ITalkClientListener listener) {
		mListener = listener;
	}
	
	public synchronized void send(byte[] data) {
		send(data, 0, data.length);
	}
	
	public synchronized void send(byte[] data, int offset, int length) {
		byte[] bs = DataCoder.encode(data, offset, length);
		mClient.sendAsync(bs, 0, bs.length);
	}
	
	public synchronized void close() {
		mClient.close();
	}
	
	public synchronized void start() {
		mParser.clear();
		
		mClient.setListener(new IClientListener() {
			@Override
			public void onReceived(NetClient client, byte[] data, int offset, int length) {
				synchronized (TalkClient.this) {
					mParser.decode(data, offset, length, new IOnDecodeListener() {
						@Override
						public void onDecode(byte[] data, int offset, int length) {
							if (mListener != null) {
								try {
									mListener.onReceived(TalkClient.this, data, offset, length);
								} catch (Exception e) {
									Logger.getInstance().print(TAG, Level.E, e);
								}
							}
						}
					});
				}
			}
			
			@Override
			public void onSended(NetClient client, boolean success) {
			}
			
			@Override
			public void onConnected(NetClient client) {
				synchronized (TalkClient.this) {
					if (mListener != null) {
						try {
							mListener.onConnected(TalkClient.this);
						} catch (Exception e) {
							Logger.getInstance().print(TAG, Level.E, e);
						}
					}
				}
			}
			
			@Override
			public void onConnecting(NetClient client) {
				synchronized (TalkClient.this) {
					if (mListener != null) {
						try {
							mListener.onConnecting(TalkClient.this);
						} catch (Exception e) {
							Logger.getInstance().print(TAG, Level.E, e);
						}
					}
				}
			}
			
			@Override
			public void onOffline(NetClient client) {
				synchronized (TalkClient.this) {
					if (mListener != null) {
						try {
							mListener.onOffline(TalkClient.this);
						} catch (Exception e) {
							Logger.getInstance().print(TAG, Level.E, e);
						}
					}
				}
			}
		});
		
		mClient.connectAsync();
	}
}

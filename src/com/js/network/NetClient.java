package com.js.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.network.NetworkUtil.Status;
import com.js.thread.ThreadUtil;

public class NetClient {
	public static final String TAG = NetClient.class.getSimpleName();
	
	protected Socket mSocket;
	protected String mHost;
	protected int mPort;
	
	protected Status mStatus = Status.Offline;
	
	protected boolean mKeepConnect = true;
	protected long mReconnectInterval = 5000;
	
	protected Runnable mReceiveRunnable;
	protected class ReceiveRunnable implements Runnable {
		@Override
		public void run() {
			try {
				InputStream is = mSocket.getInputStream();
				
				while (mStatus == Status.Connected) {
					synchronized (NetClient.this) {
						if (mReceiveRunnable != this) {
							return;
						}
					}
					
					byte[] data = new byte[1024];
					int length = is.read(data);
					
					if (length > 0) {
						notifyReceived(data, 0, length);
					} else if (mKeepConnect) {
						Thread.sleep(100);
					} else {
						break;
					}
				}
			} catch (Exception e) {
				Logger.getInstance().print(TAG, Level.E, e);
			}

			synchronized (NetClient.this) {
				if (mReceiveRunnable == this) {
					close(true);
				}
			}
		}
	};
	
	protected IClientListener mListener;
	
	public NetClient() {
	}
	
	public synchronized void setSocket(Socket socket) {
		mSocket = socket;
	}
	
	public synchronized void setHost(String host) {
		mHost = host;
	}
	
	public synchronized void setPort(int port) {
		mPort = port;
	}
	
	public synchronized Status getStatus() {
		return mStatus;
	}
	
	public synchronized void setKeepConnect(boolean keep) {
		mKeepConnect = keep;
	}
	
	public synchronized void setReconnectInterval(long interval) {
		mReconnectInterval = interval;
	}
	
	public synchronized void setListener(IClientListener listener) {
		mListener = listener;
	}
	
	public synchronized boolean connect() {
		close(false);
		
		try {
			mSocket = new Socket();
			mSocket.setTcpNoDelay(true);
			mSocket.setKeepAlive(mKeepConnect);
			
			mStatus = Status.Connecting;
			notifyConnecting();
			
			mSocket.connect(new InetSocketAddress(mHost, mPort));
			
			mStatus = Status.Connected;
			notifyConnected();
			
			mReceiveRunnable = new ReceiveRunnable();
			ThreadUtil.getVice().run(mReceiveRunnable);
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}

		close(true);
		return false;
	}
	
	public void connectAsync() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				connect();
			}
		});
	}
	
	public synchronized boolean send(byte[] data, int offset, int length) {
		try {
			OutputStream os = mSocket.getOutputStream();
			os.write(data, offset, length);
			os.flush();
			
			notifySended(true);
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		notifySended(false);
		return false;
	}
	
	public void sendAsync(final byte[] data, final int offset, final int length) {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				send(data, offset, length);
			}
		});
	}
	
	public synchronized void close() {
		mReconnectInterval = -1;
		close(false);
	}
	
	private synchronized void close(boolean reconnect) {
		NetworkUtil.closeSocket(mSocket);
		mSocket = null;
		mReceiveRunnable = null;
		
		if (mStatus != Status.Offline) {
			mStatus = Status.Offline;
			notifyOffline();
		}
		
		if (reconnect && mReconnectInterval > 0) {
			ThreadUtil.getVice().run(new Runnable() {
				@Override
				public void run() {
					synchronized (NetClient.this) {
						try {
							Thread.sleep(mReconnectInterval);
						} catch (Exception e) {
							Logger.getInstance().print(TAG, Level.E, e);
						}
						
						if (mStatus == Status.Offline && mReconnectInterval > 0) {
							connect();
						}
					}
				}
			});
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	protected void notifyConnected() {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				IClientListener listener = mListener;
				if (listener != null) {
					listener.onConnected(NetClient.this);
				}
			}
		});
	}
	
	protected void notifyConnecting() {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				IClientListener listener = mListener;
				if (listener != null) {
					listener.onConnecting(NetClient.this);
				}
			}
		});
	}
	
	protected void notifyOffline() {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				IClientListener listener = mListener;
				if (listener != null) {
					listener.onOffline(NetClient.this);
				}
			}
		});
	}
	
	protected void notifySended(final boolean success) {
		Logger.getInstance().print(TAG, Level.V, success);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				IClientListener listener = mListener;
				if (listener != null) {
					listener.onSended(NetClient.this, success);
				}
			}
		});
	}
	
	protected void notifyReceived(final byte[] data,
			final int offset, final int length) {
		
		Logger.getInstance().print(TAG, Level.V);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				IClientListener listener = mListener;
				if (listener != null) {
					listener.onReceived(NetClient.this,
							data, offset, length);
				}
			}
		});
	}
}

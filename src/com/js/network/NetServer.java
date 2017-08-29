package com.js.network;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.network.NetworkUtil.Status;
import com.js.thread.ThreadUtil;

public class NetServer {
	public static final String TAG = NetServer.class.getSimpleName();
	
	protected ServerSocket mSocket;
	protected int mPort;
	
	protected Status mStatus = Status.Offline;
	
	protected boolean mKeepConnect = true;
	protected long mRecreateInterval = 10000;
	protected long mConnectCount = 0;
	
	protected Runnable mAcceptRunnable;
	protected class AcceptRunnable implements Runnable {
		@Override
		public void run() {
			try {
				while (mStatus == Status.Connected) {
					synchronized (NetServer.this) {
						if (mAcceptRunnable != this) {
							return;
						}
					}
					
					Socket client = mSocket.accept();
					onAccept(client);
				}
			} catch (Exception e) {
				Logger.getInstance().print(TAG, Level.E, e);
			}

			synchronized (NetServer.this) {
				if (mAcceptRunnable == this) {
					close(true);
				}
			}
		}
	};
	
	protected IServerListener mListener;
	
	public NetServer() {
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
	
	public synchronized void setRecreateInterval(long interval) {
		mRecreateInterval = interval;
	}
	
	public synchronized void setListener(IServerListener listener) {
		mListener = listener;
	}
	
	public synchronized boolean connect() {
		close(false);
		
		try {
			mSocket = new ServerSocket();
			
			mStatus = Status.Connecting;
			notifyConnecting();
			
			mSocket.bind(new InetSocketAddress(mPort));
			
			mStatus = Status.Connected;
			notifyConnected();
			
			mAcceptRunnable = new AcceptRunnable();
			ThreadUtil.getVice().run(mAcceptRunnable);
			
			mConnectCount++;
			Logger.getInstance().print(TAG, Level.D, mConnectCount);
			
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
	
	public synchronized void close(boolean recreate) {
		try {
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		mSocket = null;
		mAcceptRunnable = null;
		
		if (mStatus != Status.Offline) {
			mStatus = Status.Offline;
			notifyOffline();
			
			if (recreate && mRecreateInterval > 0) {
				ThreadUtil.getVice().run(new Runnable() {
					@Override
					public void run() {
						connect();
					}
				}, mRecreateInterval);
			}
		}
	}
	
	protected void onAccept(final Socket socket) {
		final NetClient client = new NetClient();
		client.setSocket(socket);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				notifyAccepted(client);
				
				try {
					InputStream is = socket.getInputStream();
					
					while (true) {
						byte[] data = new byte[1024];
						int length = is.read(data);
						
						if (length > 0) {
							notifyReceived(client, data, length);
						} else if (mKeepConnect) {
							Thread.sleep(100);
						} else {
							break;
						}
					}
				} catch (Exception e) {
					Logger.getInstance().print(TAG, Level.E, e);
				}
				
				NetworkUtil.closeSocket(socket);
				
				notifyLeaved(client);
			}
		});
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	protected void notifyConnected() {
		Logger.getInstance().print(TAG, Level.D);
		
		IServerListener listener = mListener;
		if (listener != null) {
			listener.onConnected(NetServer.this);
		}
	}
	
	protected void notifyConnecting() {
		Logger.getInstance().print(TAG, Level.D);

		IServerListener listener = mListener;
		if (listener != null) {
			listener.onConnecting(NetServer.this);
		}
	}
	
	protected void notifyOffline() {
		Logger.getInstance().print(TAG, Level.D);
		
		IServerListener listener = mListener;
		if (listener != null) {
			listener.onOffline(NetServer.this);
		}
	}
	
	protected void notifyAccepted(final NetClient client) {
		Logger.getInstance().print(TAG, Level.D);
		
		IServerListener listener = mListener;
		if (listener != null) {
			listener.onAccepted(NetServer.this, client);
		}
	}

	protected void notifyReceived(final NetClient client,
			final byte[] data, final int length) {
		
		Logger.getInstance().print(TAG, Level.V);

		IServerListener listener = mListener;
		if (listener != null) {
			listener.onReceived(NetServer.this,
					client, data, 0, length);
		}
	}
	
	protected void notifyLeaved(final NetClient client) {
		Logger.getInstance().print(TAG, Level.D);
		
		IServerListener listener = mListener;
		if (listener != null) {
			listener.onLeaved(NetServer.this, client);
		}
	}
}

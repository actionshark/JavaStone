package com.js.network;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

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
	
	protected final Semaphore mAcceptSema = new Semaphore(0);
	protected final Runnable mAcceptRunnable = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					mAcceptSema.acquire();
					
					while (mStatus == Status.Connected) {
						Socket client = mSocket.accept();
						onAccept(client);
					}
				} catch (Exception e) {
					Logger.getInstance().print(TAG, Level.E, e);
				}
				
				close(true);
			}
		}
	};
	
	protected IServerListener mListener;
	
	public NetServer() {
		ThreadUtil.getVice().run(mAcceptRunnable);
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
			
			mAcceptSema.release();
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
		
		if (mStatus != Status.Offline) {
			mStatus = Status.Offline;
			notifyOffline();
			
			if (recreate && mRecreateInterval > 0) {
				ThreadUtil.getVice().run(new Runnable() {
					@Override
					public void run() {
						synchronized (NetServer.this) {
							try {
								Thread.sleep(mRecreateInterval);
							} catch (Exception e) {
								Logger.getInstance().print(TAG, Level.E, e);
							}
							
							connect();
						}
					}
				});
			}
		}
	}
	
	protected void onAccept(final Socket socket) {
		final NetClient client = new NetClient();
		client.setSocket(socket);
		
		notifyAccepted(client);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = socket.getInputStream();
					
					while (true) {
						byte[] data = new byte[1024];
						int length = is.read(data);
						
						if (length > 0) {
							notifyReceived(client, data, length);
						} else if (mKeepConnect) {
							Thread.sleep(200);
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
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onConnected(NetServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyConnecting() {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onConnecting(NetServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyOffline() {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onOffline(NetServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyAccepted(final NetClient client) {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onAccepted(NetServer.this, client);
					}
				}
			}
		});
	}

	protected void notifyReceived(final NetClient client,
			final byte[] data, final int length) {
		
		Logger.getInstance().print(TAG, Level.V);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onReceived(NetServer.this,
							client, data, 0, length);
					}
				}
			}
		});
	}
	
	protected void notifyLeaved(final NetClient client) {
		Logger.getInstance().print(TAG, Level.D);
		
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onLeaved(NetServer.this, client);
					}
				}
			}
		});
	}
}

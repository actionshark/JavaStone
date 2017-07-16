package com.js.network;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.thread.ThreadUtil;

public class NetServer {
	public static final String TAG = NetServer.class.getSimpleName();
	
	protected ServerSocket mSocket;
	protected int mPort;
	
	protected Status mStatus = Status.None;
	
	protected boolean mKeepConnect = true;
	protected long mRecreateInterval = 10000;
	
	protected IServerListener mListener;
	
	protected final Runnable mAcceptRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					synchronized (NetServer.this) {
						if (mStatus != Status.Connected) {
							return;
						}
					}
					
					Socket client = mSocket.accept();
					onAccept(client);
					
					Thread.sleep(100);
				}
			} catch (Exception e) {
				Logger.getInstance().print(TAG, Level.E, e);
			}
			
			synchronized (NetServer.this) {
				mStatus = Status.Disconnected;
				notifyDisconnected();
				
				if (mRecreateInterval > 0) {
					try {
						Thread.sleep(mRecreateInterval);
					} catch (Exception e) {
						Logger.getInstance().print(TAG, Level.E, e);
					}
					
					connectAsync();
				}
			}
		}
	};
	
	public NetServer() {
	}
	
	public synchronized Status getStatus() {
		return mStatus;
	}
	
	public synchronized void setPort(int port) {
		mPort = port;
	}
	
	public synchronized void setKeepConnect(boolean keep) {
		mKeepConnect = keep;
	}
	
	public synchronized void setRecreateInterval(long interval) {
		mRecreateInterval = interval;
	}
	
	public synchronized boolean connect() {
		if (mStatus != Status.None && mStatus != Status.Disconnected) {
			return false;
		}
		
		if (mStatus == Status.Disconnected) {
			close_();
		}
		
		try {
			mSocket = new ServerSocket();
			mSocket.bind(new InetSocketAddress(mPort));
			mStatus = Status.Connected;
			
			notifyConnected();
			
			ThreadUtil.getVice().run(mAcceptRunnable);
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		notifyConnectFailed();
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
	
	public synchronized boolean close() {
		if (mStatus != Status.Connected && mStatus != Status.Disconnected) {
			return false;
		}
		
		if (close_()) {
			notifyClosed();
			return true;
		} else {
			return false;
		}
	}
	
	protected synchronized boolean close_() {
		try {
			if (mSocket != null) {
				if (mSocket.isClosed() == false) {
					mSocket.close();
				}
				mSocket = null;
			}
			
			mStatus = Status.Closed;
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		return false;
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
	
	public synchronized void setListener(IServerListener listener) {
		mListener = listener;
	}
	
	protected void notifyConnected() {
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
	
	protected void notifyConnectFailed() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onConnectFailed(NetServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyDisconnected() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onDisconnected(NetServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyClosed() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onClosed(NetServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyAccepted(final NetClient client) {
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

	protected void notifyReceived(final NetClient client, final byte[] data, final int length) {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetServer.this) {
					if (mListener != null) {
						mListener.onReceived(NetServer.this, client, data, 0, length);
					}
				}
			}
		});
	}
	
	protected void notifyLeaved(final NetClient client) {
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

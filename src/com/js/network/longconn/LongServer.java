package com.js.network.longconn;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.thread.ThreadUtil;

public class LongServer {
	public static final String TAG = LongServer.class.getSimpleName();
	
	protected ServerSocket mSocket;
	protected int mPort;
	
	protected Status mStatus = Status.None;
	
	protected int mTimeout = 10;
	protected long mRecreateInterval = 10000;
	
	protected IServerListener mListener;
	
	protected final Runnable mAcceptRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					synchronized (LongServer.this) {
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
			
			synchronized (LongServer.this) {
				mStatus = Status.Disconnected;
				notifyDisconnected();
				
				if (mRecreateInterval > 0) {
					try {
						Thread.sleep(mRecreateInterval);
					} catch (Exception e) {
						Logger.getInstance().print(TAG, Level.E, e);
					}
					
					connect();
				}
			}
		}
	};
	
	public LongServer() {
	}
	
	public synchronized Status getStatus() {
		return mStatus;
	}
	
	public synchronized void setPort(int port) {
		mPort = port;
	}
	
	public synchronized void setTimeout(int timeout) {
		mTimeout = timeout;
	}
	
	public synchronized void setRecreateInterval(long interval) {
		mRecreateInterval = interval;
	}
	
	public synchronized boolean connectSync() {
		if (mStatus != Status.None && mStatus != Status.Disconnected) {
			return false;
		}
		
		if (mStatus == Status.Disconnected) {
			close_();
		}
		
		try {
			mSocket = new ServerSocket();
			mSocket.setSoTimeout(mTimeout);
			mSocket.bind(new InetSocketAddress(mPort));
			mStatus = Status.Connected;
			
			notifyConnected();
			
			ThreadUtil.run(mAcceptRunnable);
			return true;
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		notifyConnectFailed();
		return false;
	}
	
	public void connect() {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				connectSync();
			}
		});
	}
	
	public synchronized boolean close() {
		if (mStatus != Status.Connected && mStatus != Status.Disconnected) {
			return false;
		}
		
		if (close_()) {
			mStatus = Status.Closed;
			notifyClosed();
			return true;
		} else {
			return false;
		}
	}
	
	protected synchronized boolean close_() {
		try {
			if (mSocket != null) {
				mSocket.close();
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
		notifyAccepted(socket);
		
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream is = socket.getInputStream();
					
					while (true) {
						byte[] data = new byte[1024];
						int length = is.read(data);
						
						if (length > 0) {
							notifyReceived(socket, data, length);
						} else {
							Thread.sleep(100);
						}
					}
				} catch (Exception e) {
					Logger.getInstance().print(TAG, Level.E, e);
				}
				
				notifyLeaved(socket);
			}
		});
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public synchronized void setListener(IServerListener listener) {
		mListener = listener;
	}
	
	protected void notifyConnected() {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onConnected(LongServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyConnectFailed() {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onConnectFailed(LongServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyDisconnected() {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onDisconnected(LongServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyClosed() {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onClosed(LongServer.this);
					}
				}
			}
		});
	}
	
	protected void notifyAccepted(final Socket socket) {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onAccepted(LongServer.this, socket);
					}
				}
			}
		});
	}

	protected void notifyReceived(final Socket socket, final byte[] data, final int length) {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onReceived(LongServer.this, socket, data, 0, length);
					}
				}
			}
		});
	}
	
	protected void notifyLeaved(final Socket socket) {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (LongServer.this) {
					if (mListener != null) {
						mListener.onLeaved(LongServer.this, socket);
					}
				}
			}
		});
	}
}

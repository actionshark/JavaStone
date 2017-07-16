package com.js.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.thread.ThreadUtil;

public class NetClient {
	public static final String TAG = NetClient.class.getSimpleName();
	
	protected Socket mSocket;
	protected String mHost;
	protected int mPort;
	
	protected boolean mKeepConnect = true;
	protected long mReconnectInterval = 10000;
	
	protected Runnable mReceiveRunnable = null;
	protected class ReceiveRunable implements Runnable {
		@Override
		public void run() {
			try {
				synchronized (NetClient.this) {
					if (mReceiveRunnable != this) {
						return;
					}
				}
				
				InputStream is = mSocket.getInputStream();
				
				while (true) {
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
						Thread.sleep(200);
					} else {
						break;
					}
				}
			} catch (Exception e) {
				Logger.getInstance().print(TAG, Level.E, e);
			}
			
			synchronized (NetClient.this) {
				if (mReceiveRunnable != this) {
					return;
				}
				
				NetworkUtil.closeSocket(mSocket);
				notifyDisconnected();
				
				if (mReconnectInterval > 0) {
					try {
						Thread.sleep(mReconnectInterval);
					} catch (Exception e) {
						Logger.getInstance().print(TAG, Level.E, e);
					}
					
					if (mReceiveRunnable != this) {
						return;
					}
					
					connectAsync();
				}
			}
		}
	};
	
	protected IClientListener mListener;
	
	public NetClient() {
	}
	
	public synchronized Status getStatus() {
		if (mSocket == null) {
			return Status.None;
		}
		
		if (mSocket.isClosed()) {
			return Status.Closed;
		}
		
		if (mSocket.isConnected()) {
			return Status.Connected;
		}
		
		if (mSocket.isBound()) {
			return Status.Disconnected;
		}
		
		return Status.None;
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
	
	public synchronized void setKeepConnect(boolean keep) {
		mKeepConnect = keep;
	}
	
	public synchronized boolean connect() {
		NetworkUtil.closeSocket(mSocket);
		
		try {
			mSocket = new Socket();
			mSocket.setKeepAlive(mKeepConnect);
			mSocket.connect(new InetSocketAddress(mHost, mPort));
			
			notifyConnected();
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
			
			notifyConnectFailed();
			return false;
		}
		
		mReceiveRunnable = new ReceiveRunable();
		ThreadUtil.getVice().run(mReceiveRunnable);
		return true;
	}
	
	public synchronized void connectAsync() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				connect();
			}
		});
	}
	
	public boolean send(byte[] data, int offset, int length) {
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
	
	public synchronized boolean close() {
		try {
			if (mSocket != null && mSocket.isClosed() == false) {
				NetworkUtil.closeSocket(mSocket);
				notifyClosed();
				return true;
			}
		} catch (Exception e) {
			Logger.getInstance().print(TAG, Level.E, e);
		}
		
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	public synchronized void setListener(IClientListener listener) {
		mListener = listener;
	}
	
	protected void notifyConnected() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetClient.this) {
					if (mListener != null) {
						mListener.onConnected(NetClient.this);
					}
				}
			}
		});
	}
	
	protected void notifyConnectFailed() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetClient.this) {
					if (mListener != null) {
						mListener.onConnectFailed(NetClient.this);
					}
				}
			}
		});
	}
	
	protected void notifyDisconnected() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetClient.this) {
					if (mListener != null) {
						mListener.onDisconnected(NetClient.this);
					}
				}
			}
		});
	}
	
	protected void notifyClosed() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetClient.this) {
					if (mListener != null) {
						mListener.onClosed(NetClient.this);
					}
				}
			}
		});
	}
	
	protected void notifySended(final boolean success) {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetClient.this) {
					if (mListener != null) {
						mListener.onSended(NetClient.this, success);
					}
				}
			}
		});
	}
	
	protected void notifyReceived(final byte[] data, final int offset, final int length) {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (NetClient.this) {
					if (mListener != null) {
						mListener.onReceived(NetClient.this, data, offset, length);
					}
				}
			}
		});
	}
}

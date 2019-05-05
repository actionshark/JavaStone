package com.shk.js.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.network.NetworkUtil.Status;
import com.shk.js.thread.ThreadUtil;

public class NetClient {
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
				Logger.print(Level.E, e);
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
			Logger.print(Level.E, e);
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
			Logger.print(Level.E, e);
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
						if (mStatus == Status.Offline && mReconnectInterval > 0) {
							connect();
						}
					}
				}
			}, mReconnectInterval);
		}
	}

	////////////////////////////////////////////////////////////////////////////

	protected void notifyConnected() {
		Logger.print(Level.D);

		IClientListener listener = mListener;
		if (listener != null) {
			listener.onConnected(NetClient.this);
		}
	}

	protected void notifyConnecting() {
		Logger.print(Level.D);

		IClientListener listener = mListener;
		if (listener != null) {
			listener.onConnecting(NetClient.this);
		}
	}

	protected void notifyOffline() {
		Logger.print(Level.D);

		IClientListener listener = mListener;
		if (listener != null) {
			listener.onOffline(NetClient.this);
		}
	}

	protected void notifySended(final boolean success) {
		Logger.print(Level.V, success);

		IClientListener listener = mListener;
		if (listener != null) {
			listener.onSended(NetClient.this, success);
		}
	}

	protected void notifyReceived(final byte[] data, final int offset, final int length) {
		Logger.print(Level.V);

		IClientListener listener = mListener;
		if (listener != null) {
			listener.onReceived(NetClient.this, data, offset, length);
		}
	}
}

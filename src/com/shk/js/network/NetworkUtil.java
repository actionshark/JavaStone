package com.shk.js.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkUtil {
	public static enum Status {
		Offline, Connecting, Connected,
	}

	public static void closeSocket(Socket socket) {
		try {
			if (socket == null) {
				return;
			}

			try {
				socket.shutdownInput();
			} catch (Exception e) {
			}
			try {
				socket.shutdownOutput();
			} catch (Exception e) {
			}

			try {
				InputStream is = socket.getInputStream();
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}

			try {
				OutputStream os = socket.getOutputStream();
				if (os != null) {
					os.close();
				}
			} catch (Exception e) {
			}

			try {
				socket.close();
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}
	}
}

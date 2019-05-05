package com.shk.js.talk;

public interface ITalkServerListener<T extends UserInfo> {
	public void onReceived(TalkServer<T> server, TalkClient client,
		T userInfo, byte[] data, int offset, int length);
	public void onLeaved(TalkServer<T> server, TalkClient client, T userInfo);
}

package com.js.talk;

import net.sf.json.JSONObject;

public interface ITalkServerListener<T extends UserInfo> {
	public void onReceived(TalkServer<T> server, T userInfo, JSONObject jo);
	public void onLeaved(TalkServer<T> server, T userInfo);
}

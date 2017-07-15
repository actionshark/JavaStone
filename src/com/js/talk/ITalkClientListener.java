package com.js.talk;

import net.sf.json.JSONObject;

public interface ITalkClientListener {
	public void onReceived(TalkClient client, JSONObject jo);
}

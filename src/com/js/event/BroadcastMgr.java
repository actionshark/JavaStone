package com.js.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.thread.ThreadUtil;

public class BroadcastMgr {
	public static final String TAG = BroadcastMgr.class.getSimpleName();
	
	private static BroadcastMgr sInstance = new BroadcastMgr();
	
	public static BroadcastMgr getInstance() {
		return sInstance;
	}
	
	private final Map<String, List<Object>> mEvents = new HashMap<String, List<Object>>();
	
	private final Map<String, List<IBroadcastListener>> mListeners = new HashMap<String, List<IBroadcastListener>>();
	
	public synchronized void send(String name, Object data) {
		List<Object> list = mEvents.get(name);
		
		if (list == null) {
			list = new ArrayList<Object>();
			mEvents.put(name, list);
		}
		
		list.add(data);
		
		run();
	}
	
	public synchronized void addListener(String name, IBroadcastListener listener) {
		List<IBroadcastListener> list = mListeners.get(name);
		
		if (list == null) {
			list = new ArrayList<IBroadcastListener>();
			mListeners.put(name, list);
		}
		
		list.add(listener);
	}
	
	public synchronized int removeListener(String name, IBroadcastListener listener) {
		List<IBroadcastListener> list = mListeners.get(name);
		
		if (list == null) {
			return 0;
		}
		
		int count = 0;
		
		for (int i = list.size() - 1; i >= 0; i--) {
			IBroadcastListener lt = list.get(i);
			
			if (lt == listener) {
				list.remove(i);
				count++;
			}
		}
		
		return count;
	}
	
	private void run() {
		ThreadUtil.getVice().run(new Runnable() {
			@Override
			public void run() {
				synchronized (BroadcastMgr.this) {
					for (Entry<String, List<Object>> events : mEvents.entrySet()) {
						final String name = events.getKey();
						List<Object> datas = events.getValue();
						
						List<IBroadcastListener> listeners = mListeners.get(name);
						if (listeners == null) {
							continue;
						}
						
						for (final IBroadcastListener listener : listeners) {
							for (final Object data : datas) {
								ThreadUtil.getVice().run(new Runnable() {
									@Override
									public void run() {
										try {
											listener.onBroadcast(name, data);
										} catch (Exception e) {
											Logger.getInstance().print(TAG, Level.E, e);
										}
									}
								});
							}
						}
					}
					
					mEvents.clear();
				}
			}
		});
	}
}

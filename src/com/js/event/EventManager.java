package com.js.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.js.log.Level;
import com.js.log.Logger;
import com.js.thread.ThreadUtil;

public class EventManager {
	public static final String TAG = EventManager.class.getSimpleName();
	
	private static EventManager sEventManager = new EventManager();
	
	public static EventManager getInstance() {
		return sEventManager;
	}
	
	private final Map<String, List<Object>> mEvents = new HashMap<String, List<Object>>();
	
	private final Map<String, List<IEventListener>> mListeners = new HashMap<String, List<IEventListener>>();
	
	public synchronized void send(String name, Object data) {
		List<Object> list = mEvents.get(name);
		
		if (list == null) {
			list = new ArrayList<Object>();
			mEvents.put(name, list);
		}
		
		list.add(data);
		
		run();
	}
	
	public synchronized void addListener(String name, boolean inMainThread, IEventListener listener) {
		List<IEventListener> list = mListeners.get(name);
		
		if (list == null) {
			list = new ArrayList<IEventListener>();
			mListeners.put(name, list);
		}
		
		list.add(listener);
	}
	
	public synchronized int removeListener(String name, IEventListener listener) {
		List<IEventListener> list = mListeners.get(name);
		
		if (list == null) {
			return 0;
		}
		
		int count = 0;
		
		for (int i = list.size() - 1; i >= 0; i--) {
			IEventListener lt = list.get(i);
			
			if (lt == listener) {
				list.remove(i);
				count++;
			}
		}
		
		return count;
	}
	
	private void run() {
		ThreadUtil.run(new Runnable() {
			@Override
			public void run() {
				synchronized (EventManager.this) {
					for (Entry<String, List<Object>> events : mEvents.entrySet()) {
						final String name = events.getKey();
						List<Object> datas = events.getValue();
						
						List<IEventListener> listeners = mListeners.get(name);
						if (listeners != null) {
							for (final IEventListener listener : listeners) {
								for (final Object data : datas) {
									ThreadUtil.run(new Runnable() {
										@Override
										public void run() {
											try {
												listener.onEvent(name, data);
											} catch (Exception e) {
												Logger.getInstance().print(TAG, Level.E, e);
											}
										}
									});
								}
							}
						}
					}
					
					mEvents.clear();
				}
			}
		});
	}
}

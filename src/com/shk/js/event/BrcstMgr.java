package com.shk.js.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;
import com.shk.js.thread.ThreadUtil;

public class BrcstMgr {
	private static BrcstMgr sInstance = new BrcstMgr();

	public static BrcstMgr getInstance() {
		return sInstance;
	}

	private static class ListenerNode {
		public IBrcstListener listener;
	}

	private final Map<String, List<Object>> mEvents = new HashMap<String, List<Object>>();

	private final Map<String, List<ListenerNode>> mListeners = new HashMap<String, List<ListenerNode>>();

	public synchronized void send(String name, Object data) {
		List<Object> list = mEvents.get(name);

		if (list == null) {
			list = new ArrayList<Object>();
			mEvents.put(name, list);
		}

		list.add(data);

		run();
	}

	public synchronized void addListener(String name, IBrcstListener listener) {
		List<ListenerNode> list = mListeners.get(name);

		if (list == null) {
			list = new ArrayList<ListenerNode>();
			mListeners.put(name, list);
		}

		ListenerNode ln = new ListenerNode();
		ln.listener = listener;

		list.add(ln);
	}

	public synchronized void removeListener(String name, IBrcstListener listener) {
		if (name == null) {
			for (List<ListenerNode> lns : mListeners.values()) {
				removeListener(lns, listener);
			}
		} else {
			List<ListenerNode> lns = mListeners.get(name);
			if (lns != null) {
				removeListener(lns, listener);
			}
		}
	}

	private void removeListener(List<ListenerNode> lns, IBrcstListener listener) {
		for (int i = lns.size() - 1; i >= 0; i--) {
			ListenerNode ln = lns.get(i);

			if (ln.listener == listener) {
				lns.remove(i);
			}
		}
	}

	private void run() {
		ThreadUtil.getInstance().run(new Runnable() {
			@Override
			public void run() {
				Map<String, List<Object>> events;
				Map<String, List<ListenerNode>> listeners;

				synchronized (BrcstMgr.this) {
					events = new HashMap<String, List<Object>>(mEvents);
					mEvents.clear();
					listeners = new HashMap<String, List<ListenerNode>>(mListeners);
				}

				for (Entry<String, List<Object>> event : events.entrySet()) {
					final String name = event.getKey();

					List<ListenerNode> lns = listeners.get(name);
					if (lns == null) {
						continue;
					}

					List<Object> datas = event.getValue();

					for (final ListenerNode ln : lns) {
						for (final Object data : datas) {
							ThreadUtil tu = ThreadUtil.getInstance();

							tu.run(new Runnable() {
								@Override
								public void run() {
									try {
										ln.listener.onBroadcast(name, data);
									} catch (Exception e) {
										Logger.print(Level.E, e);
									}
								}
							});
						}
					}
				}
			}
		});
	}
}

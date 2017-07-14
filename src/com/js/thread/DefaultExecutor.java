package com.js.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.js.log.Level;
import com.js.log.Logger;

public class DefaultExecutor implements IExecutor {
	public static final String TAG = DefaultExecutor.class.getSimpleName();
	
	private final ExecutorService mService;
	
	public DefaultExecutor() {
		mService = Executors.newCachedThreadPool();
	}

	@Override
	public void run(final Runnable runnable, final long delay) {
		mService.submit(new Runnable() {
			@Override
			public void run() {
				ThreadUtil.sleep(delay);
				
				try {
					runnable.run();
				} catch (Exception e) {
					Logger.getInstance().print(TAG, Level.E, e);
				}
			}
		});
	}
}

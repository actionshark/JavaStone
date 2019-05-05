package com.shk.js.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.shk.js.log.Level;
import com.shk.js.log.Logger;

public class DefaultExecutor implements IExecutor {
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
					Logger.print(Level.E, e);
				}
			}
		});
	}
}

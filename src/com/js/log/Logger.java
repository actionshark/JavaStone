package com.js.log;

import java.util.Calendar;

public abstract class Logger {
	private static Logger sInstance = new SimpleLogger();
	
	public static synchronized void setInstance(Logger logger) {
		sInstance = logger;
	}
	
	public static synchronized Logger getInstance() {
		return sInstance;
	}
	
	//////////////////////////////////////////////////////////////
	
	protected String mDefaultTag = "DEBUG";
	
	protected Level mLevelLimit = Level.D;
	
	public void setDefaultTag(String tag) {
		mDefaultTag = tag;
	}
	
	public void setLevelLimit(Level limit) {
		mLevelLimit = limit;
	}
	
	protected abstract void onPrint(String tag, Level level, String content) throws Exception;
	
	protected void printInternal(String tag, Level level, String content) {
		if (level.getId() < mLevelLimit.getId()) {
			return;
		}
		
		if (tag == null) {
			tag = mDefaultTag;
		}
		
		StringBuilder sb = new StringBuilder();
		
		Calendar cal = Calendar.getInstance();
		sb.append(String.format("%02d-%02d %02d:%02d:%02d ",
			cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
			cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
			cal.get(Calendar.SECOND)));
		
		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (stack.length > 2) {
			StackTraceElement element = stack[2];
			
			sb.append(element.getFileName()).append('-').append(element.getLineNumber())
				.append(':').append(element.getMethodName()).append("()\n");
		}
		
		sb.append(content).append('\n');
		
		try {
			onPrint(tag, level, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(String tag, Level level, Object... content) {
		StringBuilder sb = new StringBuilder();
		
		for (Object ct : content) {
			sb.append(ct).append(' ');
			
			if (ct instanceof Throwable) {
				Throwable throwable = (Throwable) ct;
				StackTraceElement[] stack = throwable.getStackTrace();
				
				sb.append('\n');
				
				for (int i = 0; i < stack.length; i++) {
					StackTraceElement element = stack[i];
					
					sb.append(element.getFileName()).append('-')
						.append(element.getLineNumber()).append(':')
						.append(element.getMethodName()).append("()\n");
				}
			}
		}
		
		printInternal(tag, level, sb.toString());
	}
	
	public void printf(String tag, Level level, String format, Object... args) {
		printInternal(tag, level, String.format(format, args));
	}
	
	public void prints(String tag, Level level) {
		StackTraceElement[] stack = new Throwable().getStackTrace();
		StringBuilder sb = new StringBuilder();
		
		sb.append('\n');
		
		for (int i = 0; i < stack.length; i++) {
			StackTraceElement element = stack[i];
			
			sb.append(element.getFileName()).append('-')
				.append(element.getLineNumber()).append(':')
				.append(element.getMethodName()).append("()\n");
		}
		
		printInternal(tag, level, sb.toString());
	}
}

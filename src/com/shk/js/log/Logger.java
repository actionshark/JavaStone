package com.shk.js.log;

import java.util.Calendar;

public abstract class Logger {
	private static Logger sInstance = new SimpleLogger();

	public static void setInstance(Logger logger) {
		sInstance = logger;
	}

	private static Level sLevel = Level.D;

	public static void setLevel(Level level) {
		if (level != null) {
			sLevel = level;
		}
	}

	public static void print(Level level, Object... content) {
		sInstance.onPrintNormal(level, content);
	}

	public static void printf(Level level, String format, Object... args) {
		sInstance.onPrintFormat(level, format, args);
	}

	public static void prints(Level level) {
		sInstance.onPrintStack(level);
	}

	//////////////////////////////////////////////////////////////

	protected void onPrintNormal(Level level, Object... content) {
		StringBuilder sb = new StringBuilder();

		for (Object ct : content) {
			sb.append(ct).append(' ');

			if (ct instanceof Throwable) {
				Throwable throwable = (Throwable) ct;
				StackTraceElement[] stack = throwable.getStackTrace();

				sb.append('\n');

				for (int i = 0; i < stack.length; i++) {
					StackTraceElement element = stack[i];

					sb.append(element.getFileName()).append('-').append(element.getLineNumber()).append(':')
							.append(element.getMethodName()).append("()\n");
				}
			}
		}

		attachInfo(level, sb.toString());
	}

	protected void onPrintFormat(Level level, String format, Object... args) {
		attachInfo(level, String.format(format, args));
	}

	protected void onPrintStack(Level level) {
		StackTraceElement[] stack = new Throwable().getStackTrace();
		StringBuilder sb = new StringBuilder();

		sb.append('\n');

		for (int i = 0; i < stack.length; i++) {
			StackTraceElement element = stack[i];

			sb.append(element.getFileName()).append('-').append(element.getLineNumber()).append(':')
					.append(element.getMethodName()).append("()\n");
		}

		attachInfo(level, sb.toString());
	}

	protected void attachInfo(Level level, String content) {
		if (level.getId() < sLevel.getId()) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		Calendar cal = Calendar.getInstance();
		sb.append(
				String.format("%02d-%02d %02d:%02d:%02d ", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
						cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

		StackTraceElement[] stack = new Throwable().getStackTrace();
		if (stack.length > 3) {
			StackTraceElement element = stack[3];

			sb.append(element.getFileName()).append('-').append(element.getLineNumber()).append(':')
					.append(element.getMethodName()).append("()\n");
		}

		sb.append(content).append('\n');

		try {
			onPrint(level, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void onPrint(Level level, String content) throws Exception;
}

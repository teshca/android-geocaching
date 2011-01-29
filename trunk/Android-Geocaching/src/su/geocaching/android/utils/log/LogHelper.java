package su.geocaching.android.utils.log;

/**
 * Helper for logging
 * 
 * Used pattern "State"
 * 
 * @author Nikita Bumakov
 */
public class LogHelper {

	private static LoggingState loggingState = new LoggingState();
	private static NoLoggingState noLoggingState = new NoLoggingState();
	private static OneTagState oneTagState = new OneTagState();
	// TODO
	private static State currentLogState = loggingState;

	public static void setLogging(LogState state) {

		switch (state) {
		case Logging:
			currentLogState = loggingState;
			break;
		case NoLogging:
			currentLogState = noLoggingState;
			break;
		case OneTagLogging:
			currentLogState = oneTagState;
			break;
		}
	}

	/**
	 * Send a DEBUG log message.
	 */
	public static void d(String tag, String msg) {
		currentLogState.d(tag, msg);
	}

	/**
	 * Send an INFO log message.
	 */
	public static void i(String tag, String msg) {
		currentLogState.i(tag, msg);
	}

	/**
	 * Send a WARN log message.
	 */
	public static void w(String tag, String msg) {
		currentLogState.w(tag, msg);
	}

	/**
	 * Send a WARN log message.
	 */
	public static void w(String tag, String msg, Throwable ex) {
		currentLogState.w(tag, msg, ex);
	}

	/**
	 * Send a ERROR log message.
	 */
	public static void e(String tag, String msg) {
		currentLogState.e(tag, msg);
	}

	/**
	 * Send a ERROR log message.
	 */
	public static void e(String tag, String msg, Throwable ex) {
		currentLogState.e(tag, msg, ex);
	}

}

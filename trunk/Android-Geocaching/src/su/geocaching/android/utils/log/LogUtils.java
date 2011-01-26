package su.geocaching.android.utils.log;

/**
 * Helper for logging
 * 
 * Used pattern "State"
 * 
 * @author Nikita Bumakov
 */
public class LogUtils {

    private static boolean isLogging = true;
    private static LoggingState loggingState = new LoggingState();
    private static NoLoggingState noLoggingState = new NoLoggingState();
    private static LogState currentLogState = loggingState;

    public static boolean isLogging() {
	return isLogging;
    }

    public static void setLogging(boolean isLogging) {
	LogUtils.isLogging = isLogging;
	if (isLogging) {
	    currentLogState = loggingState;
	} else {
	    currentLogState = noLoggingState;
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

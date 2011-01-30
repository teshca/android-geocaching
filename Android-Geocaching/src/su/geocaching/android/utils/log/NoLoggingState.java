package su.geocaching.android.utils.log;

/**
 * State of no logging
 * 
 * @author Nikita Bumakov
 */
public class NoLoggingState extends State {

	@Override
	void d(String tag, String msg) {
	}

	@Override
	void i(String tag, String msg) {
	}

	@Override
	void w(String tag, String msg) {
	}

	@Override
	void w(String tag, String msg, Throwable tr) {
	}

	@Override
	void e(String tag, String msg) {
	}

	@Override
	void e(String tag, String msg, Throwable tr) {
	}

}

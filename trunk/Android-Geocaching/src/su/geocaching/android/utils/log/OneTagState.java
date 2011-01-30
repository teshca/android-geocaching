package su.geocaching.android.utils.log;

import android.util.Log;

/**
 * Logging with a single tag (Geocaching.su) for the application ;
 * 
 * @author Nikita Bumakov
 */
public class OneTagState extends State {
	private static final String TAG = "Geocaching.su";

	@Override
	void d(String tag, String msg) {
		Log.d(TAG, msg);
	}

	@Override
	void i(String tag, String msg) {
		Log.i(TAG, msg);
	}

	@Override
	void w(String tag, String msg) {
		Log.w(TAG, msg);
	}

	@Override
	void w(String tag, String msg, Throwable tr) {
		Log.w(TAG, msg, tr);
	}

	@Override
	void e(String tag, String msg) {
		Log.e(TAG, msg);
	}

	@Override
	void e(String tag, String msg, Throwable tr) {
		Log.e(TAG, msg, tr);
	}

}

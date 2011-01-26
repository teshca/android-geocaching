package su.geocaching.android.utils.log;

import android.util.Log;

class LoggingState extends LogState {

    @Override
    void d(String tag, String msg) {
	Log.d(tag, msg);
    }

    @Override
    void i(String tag, String msg) {
	Log.i(tag, msg);
    }

    @Override
    void w(String tag, String msg) {
	Log.w(tag, msg);
    }

    @Override
    void w(String tag, String msg, Throwable tr) {
	Log.w(tag, msg, tr);
    }

    @Override
    void e(String tag, String msg) {
	Log.e(tag, msg);
    }

    @Override
    void e(String tag, String msg, Throwable tr) {
	Log.e(tag, msg, tr);
    }
}

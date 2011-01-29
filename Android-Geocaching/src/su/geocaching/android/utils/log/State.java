package su.geocaching.android.utils.log;

/**
 * @author Nikita Bumakov
 */
abstract class State {

	abstract void d(String tag, String msg);

	abstract void i(String tag, String msg);

	abstract void w(String tag, String msg);

	abstract void w(String tag, String msg, Throwable tr);

	abstract void e(String tag, String msg);

	abstract void e(String tag, String msg, Throwable tr);

}

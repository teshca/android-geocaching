package su.geocaching.android.utils.log;

public class NoLoggingState extends LogState {

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

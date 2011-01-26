package su.geocaching.android.utils.log;


abstract class LogState {

    abstract void d(String tag, String msg);

    abstract void i(String tag, String msg); 

    abstract void w(String tag, String msg); 

    abstract void w(String tag, String msg, Throwable tr); 

    abstract void e(String tag, String msg); 

    abstract void e(String tag, String msg, Throwable tr);

}

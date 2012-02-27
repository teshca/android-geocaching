package su.geocaching.android.controller.managers;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manager which receive messages from different threads, tasks, etc and forward them (messages) to subscribers
 *
 * @author: Grigory Kalabin
 * @since: 11.10.2011
 */
public class CallbackManager {
    public static final int WHAT_LOCATION_DEPRECATED = 1000;

    private Collection<Handler> handlers;

    public CallbackManager() {
        handlers = new ArrayList<Handler>();
    }

    /**
     * @param handler subscriber which will be receive messages
     */
    public synchronized void addSubscriber(Handler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    /**
     * @param handler subscriber which no need to receive messages
     * @return true if subscriber has been subscribed on receiving messages
     */
    public synchronized boolean removeSubscriber(Handler handler) {
        return handlers.remove(handler);
    }

    /**
     * Send message to subscribers
     *
     * @param what code of message
     * @param obj  data which will be assigned to Message.obj field
     */
    public void postHandlerMessage(final int what, final Object obj) {
        for (Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(what, obj));
        }
    }

    /**
     * Send message without data to subscribers
     *
     * @param what code of message
     */
    public void postEmptyMessage(final int what) {
        for (Handler handler : handlers) {
            handler.sendEmptyMessage(what);
        }
    }

    /**
     * Send message to subscribers with delay
     *
     * @param what  code of message
     * @param delay of sending in milliseconds
     */
    public void postEmptyMessageDelayed(final int what, long delay) {
        for (Handler handler : handlers) {
            handler.sendEmptyMessageDelayed(what, delay);
        }
    }
}

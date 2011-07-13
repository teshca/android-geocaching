package su.geocaching.android.controller.managers;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import su.geocaching.android.controller.ConnectionStateReceiver;

/**
 * This class manage classes (named subscribers) which want to get messages about InternetConnectionState
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since December 2010
 */
public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getCanonicalName();

    private static final int SEND_MESSAGE_MIN_INTERVAL = 1000; // sending message rarely than this interval in milliseconds

    private List<IInternetAware> subscribers;
    private ConnectionStateReceiver receiver;
    private IntentFilter intentFilter;
    private Context context;
    private long lastMessageTime;
    private PingManager pingManager;

    private ConnectivityManager connectivityManager;

    /**
     * @param context
     *            which can give ConnectivityManager
     */
    public ConnectionManager(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.context = context;
        lastMessageTime = -1;
        subscribers = new ArrayList<IInternetAware>();
        receiver = new ConnectionStateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        pingManager = new PingManager();
        LogManager.d(TAG, "Init");
    }

    /**
     * @param activity
     *            which will be added
     */
    public void addSubscriber(IInternetAware activity) {
        if (subscribers.contains(activity)) {
            LogManager.w(TAG, "add subscriber: already added. Not change list. Count of list " + Integer.toString(subscribers.size()));
            return;
        }
        if (subscribers.size() == 0) {
            addUpdates();
        }
        subscribers.add(activity);
        LogManager.d(TAG, "add subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
    }

    /**
     * @param activity
     *            which will be removed
     * @return true if that activity has been contain in list of subscribers
     */
    public boolean removeSubscriber(IInternetAware activity) {
        if (subscribers.size() == 0) {
            LogManager.w(TAG, "remove subscriber: empty list. do nothing");
            return false;
        }
        boolean res = subscribers.remove(activity);
        LogManager.d(TAG, "remove subscriber. Count of subscribers became " + Integer.toString(subscribers.size()) + "; list changed=" + res);
        if (subscribers.size() == 0) {
            removeUpdates();
        }
        return res;
    }

    /**
     * Send messages to all activities when internet has been lost
     */
    public void onInternetLost() {
        pingManager.stop();
        if (Calendar.getInstance().getTimeInMillis() - lastMessageTime < SEND_MESSAGE_MIN_INTERVAL) {
            LogManager.d(TAG, "get very often message about connection. Message haven't send");
            return;
        }
        lastMessageTime = Calendar.getInstance().getTimeInMillis();
        LogManager.d(TAG, "internet lost. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
        for (IInternetAware subscriber : subscribers) {
            subscriber.onInternetLost();
        }
    }

    /**
     * starts ping manager
     */
    public void onInternetFound() {
        pingManager.start();
    }

    /**
     * @return true if internet connected
     */
    public boolean isInternetConnected() {
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnected();
        if (!isConnected) {
            return false;
        }
        return pingManager.isInternetConnected();
    }

    /**
     * Add updates of connection state
     */
    private void addUpdates() {
        context.registerReceiver(receiver, intentFilter);
        LogManager.d(TAG, "add updates");
    }

    /**
     * Remove updates of connection state
     */
    private void removeUpdates() {
        context.unregisterReceiver(receiver);
        LogManager.d(TAG, "remove updates");
    }
}

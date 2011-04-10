package su.geocaching.android.controller;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private ConnectivityManager connectivityManager;
    private ConnectionStateReceiver receiver;
    private IntentFilter intentFilter;
    private Context context;
    private long lastMessageTime;

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
     * Send messages to all activities when internet has been found
     */
    public void onInternetFound() {
        if (Calendar.getInstance().getTimeInMillis() - lastMessageTime < SEND_MESSAGE_MIN_INTERVAL) {
            LogManager.d(TAG, "get very often message about connection. Message haven't send");
            return;
        }
        lastMessageTime = Calendar.getInstance().getTimeInMillis();
        LogManager.d(TAG, "internet found. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
        for (IInternetAware subscriber : subscribers) {
            subscriber.onInternetFound();
        }
    }

    /**
     * Send messages to all activities when internet has been lost
     */
    public void onInternetLost() {
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
     * @return true if internet connected
     */
    public boolean isInternetConnected() {
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
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

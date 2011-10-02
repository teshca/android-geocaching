package su.geocaching.android.controller.managers;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
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

    private List<IConnectionAware> subscribers;
    private ConnectionStateReceiver receiver;
    private IntentFilter intentFilter;
    private Context context;

    private ConnectivityManager connectivityManager;

    /**
     * @param context
     *            which can give ConnectivityManager
     */
    public ConnectionManager(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.context = context;
        subscribers = new ArrayList<IConnectionAware>();
        receiver = new ConnectionStateReceiver();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LogManager.d(TAG, "Created");
    }

    /**
     * @param activity
     *            which will be added
     */
    public synchronized void  addSubscriber(IConnectionAware activity) {
        if (subscribers.contains(activity)) {
            LogManager.w(TAG, "add subscriber: already added. Not change list. Count of list " + Integer.toString(subscribers.size()));
            return;
        }
        if (subscribers.size() == 0) {
            registerReceiver();
        }
        subscribers.add(activity);
        LogManager.d(TAG, "add subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
    }

    /**
     * @param activity
     *            which will be removed
     * @return true if that activity has been contain in list of subscribers
     */
    public synchronized boolean removeSubscriber(IConnectionAware activity) {
        if (subscribers.size() == 0) {
            LogManager.w(TAG, "remove subscriber: empty list. do nothing");
            return false;
        }
        boolean res = subscribers.remove(activity);
        LogManager.d(TAG, "remove subscriber. Count of subscribers became " + Integer.toString(subscribers.size()) + "; list changed=" + res);
        if (subscribers.size() == 0) {
            unregisterReceiver();
        }
        return res;
    }

    /**
     * Send messages to all activities when connection is lost
     */
    public void onConnectionLost() {
        LogManager.d(TAG, "connection lost. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
        for (IConnectionAware subscriber : subscribers) {
            subscriber.onConnectionLost();
        }
    }

    /**
     * Send messages to all activities when connection is found
     */
    public void onConnectionFound() {
        LogManager.d(TAG, "connection found. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
        for (IConnectionAware subscriber : subscribers) {
            subscriber.onConnectionFound();
        }
    }

    public boolean isActiveNetworkConnected() {
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
    }

    /**
     * Add updates of connection state
     */
    private void registerReceiver() {
        context.registerReceiver(receiver, intentFilter);
        LogManager.d(TAG, "register receiver");
    }

    /**
     * Remove updates of connection state
     */
    private void unregisterReceiver() {
        context.unregisterReceiver(receiver);
        LogManager.d(TAG, "unregister receiver");
    }
}

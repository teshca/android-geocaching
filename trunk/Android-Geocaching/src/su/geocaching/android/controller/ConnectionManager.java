package su.geocaching.android.controller;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manage classes (named subscribers) which want to get messages about InternetConnectionState
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since December 2010
 */
public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getCanonicalName();

    private static final String PING_URL = "http://pda.geocaching.su";

    private List<IInternetAware> subscribers;
    private ConnectivityManager connectivityManager;
    private ConnectionStateReceiver receiver;
    private IntentFilter intentFilter;
    private Context context;
    private URL pingUrl;

    /**
     * @param context //TODO describe it
     */
    public ConnectionManager(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.context = context;
        subscribers = new ArrayList<IInternetAware>();
        receiver = new ConnectionStateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        try {
            pingUrl = new URL(PING_URL);
        } catch (MalformedURLException e) {
            LogManager.e(TAG, "ConnectionManager init: malformed url (" + PING_URL + ")");
            e.printStackTrace();
        }
        LogManager.d(TAG, "Init");
    }

    /**
     * @param activity which will be added
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
     * @param activity which will be removed
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
        LogManager.d(TAG, "internet found. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
        for (IInternetAware subscriber : subscribers) {
            subscriber.onInternetFound();
        }
    }

    /**
     * Send messages to all activities when internet has been lost
     */
    public void onInternetLost() {
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
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnected();
        if (!isConnected) {
            return false;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) pingUrl.openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                isConnected = false;
                LogManager.d(TAG, "Check connection: not reachable (" + PING_URL + ") Response: " + connection.getResponseCode());
            } else {
                LogManager.d(TAG, "Check connection: reachable (" + PING_URL + ")");
            }
        } catch (IOException e) {
            isConnected = false;
            LogManager.w(TAG, "Check connection: IO exception (" + PING_URL + ")", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return isConnected;
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

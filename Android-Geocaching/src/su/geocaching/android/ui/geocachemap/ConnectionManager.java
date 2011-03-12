package su.geocaching.android.ui.geocachemap;

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

import su.geocaching.android.controller.LogManager;

/**
 * This class manage classes (named subscribers) which want to get messages about InternetConnectionState
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since December 2010
 */
public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getCanonicalName();

    private static final String CONTROL_URL = "http://pda.geocaching.su";

    private List<IInternetAware> subscribers;
    private ConnectivityManager connectivityManager;
    private ConnectionStateReceiver reciever;
    private IntentFilter intentFilter;
    private Context context;

    /**
     * @param connectivityManager
     *            manager which know about network state
     */
    public ConnectionManager(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.context = context;
        subscribers = new ArrayList<IInternetAware>();
        reciever = new ConnectionStateReceiver();
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
        LogManager.d(TAG, "remove subscriber. Count of subscribers was " + Integer.toString(subscribers.size()));
        boolean res = subscribers.remove(activity);
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
        boolean res =  activeNetInfo != null && activeNetInfo.isConnected();
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(CONTROL_URL);
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                res = false;
                LogManager.d(TAG, "Check connection: not reachable ("+CONTROL_URL+") Response: " + connection.getResponseCode());
            } else {
                LogManager.d(TAG, "Check connection: reachable ("+CONTROL_URL+")");
            }
            connection.disconnect();
        } catch (MalformedURLException e) {
            LogManager.e(TAG, "Check connection: mailformed url ("+CONTROL_URL+")");
            e.printStackTrace();
            res = false;
        } catch (IOException e) {
            res = false;
            LogManager.w(TAG, "Check connection: IO exception ("+CONTROL_URL+")",e);
        }
        return res;
    }

    /**
     * Add updates of connection state
     */
    private void addUpdates() {
        context.registerReceiver(reciever, intentFilter);
        LogManager.d(TAG, "add updates");
    }

    /**
     * Remove updates of connection state
     */
    private void removeUpdates() {
        context.unregisterReceiver(reciever);
        LogManager.d(TAG, "remove updates");
    }
}

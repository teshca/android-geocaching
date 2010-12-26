package su.geocaching.android.ui.geocachemap;

import java.util.ArrayList;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * This class manage classes (named subscribers) which want to get messages
 * about InternetConnectionState
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Dec 2, 2010
 * 
 */
public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getCanonicalName();

    private List<IInternetAware> subscribers;
    private ConnectivityManager connectivityManager;

    /**
     * @param connectivityManager
     *            manager which know about network state
     */
    public ConnectionManager(ConnectivityManager connectivityManager) {
	this.connectivityManager = connectivityManager;
	subscribers = new ArrayList<IInternetAware>();
	Log.d(TAG, "Init");
    }

    /**
     * @param activity
     *            which will be added
     */
    public void addSubscriber(IInternetAware activity) {
	if (subscribers.contains(activity)) {
	    Log.w(TAG, "add subscriber: already added. Not change list. Count of list " + Integer.toString(subscribers.size()));
	    return;
	}
	subscribers.add(activity);
	Log.d(TAG, "add subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
    }

    /**
     * @param activity
     *            which will be removed
     * @return true if that activity has been contain in list of subscribers
     */
    public boolean removeSubscriber(IInternetAware activity) {
	Log.d(TAG, "remove subscriber. Count of subscribers was " + Integer.toString(subscribers.size()));
	return subscribers.remove(activity);
    }

    /**
     * Send messages to all activities when internet has been found
     */
    public void onInternetFound() {
	Log.d(TAG, "internet found. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
	for (IInternetAware subscriber : subscribers) {
	    subscriber.onInternetFound();
	}
    }

    /**
     * Send messages to all activities when internet has been lost
     */
    public void onInternetLost() {
	Log.d(TAG, "internet lost. Send msg to " + Integer.toString(subscribers.size()) + " subscribers");
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
}

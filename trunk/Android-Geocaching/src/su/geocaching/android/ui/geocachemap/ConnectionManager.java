package su.geocaching.android.ui.geocachemap;

import java.util.ArrayList;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionManager {
    private List<IInternetAware> subscribers;
    private ConnectivityManager connectivityManager;

    public ConnectionManager(ConnectivityManager connectivityManager) {
	this.connectivityManager = connectivityManager;
	subscribers = new ArrayList<IInternetAware>();
    }

    public void addSubscriber(IInternetAware activity) {
	subscribers.add(activity);
    }

    public boolean removeSubscriber(IInternetAware activity) {
	return subscribers.remove(activity);
    }

    public void onInternetFound() {
	for (IInternetAware subscriber : subscribers) {
	    subscriber.onInternetFound();
	}
    }

    public void onInternetLost() {
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

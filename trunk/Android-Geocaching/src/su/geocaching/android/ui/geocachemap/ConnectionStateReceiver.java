package su.geocaching.android.ui.geocachemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 22, 2010
 *      <p>
 *      used for listen broadcast messages for activities which uses internet
 *      </p>
 */
public class ConnectionStateReceiver extends BroadcastReceiver {
    private IInternetAware context;

    /**
     * @param context
     *            activity which use internet
     */
    public ConnectionStateReceiver(IInternetAware context) {
	this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
	if (isInternetConnected()) {
	    this.context.onInternetFound();
	} else {
	    this.context.onInternetLost();
	}
    }

    /**
     * @return true if internet connected
     */
    public boolean isInternetConnected() {
	ConnectivityManager connectivityManager = (ConnectivityManager) ((Context) context).getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	return activeNetInfo != null && activeNetInfo.isConnected();
    }
}

package su.geocaching.android.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import su.geocaching.android.controller.managers.ConnectionManager;
import su.geocaching.android.controller.managers.LogManager;

/**
 * Used for listen broadcast messages for activities which uses internet. It
 * shouldn't be instantiate.
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 22, 2010
 */
public class ConnectionStateReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectionStateReceiver.class.getCanonicalName();

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        LogManager.d(TAG, "Recieved message about internet status");
        ConnectionManager connManager = Controller.getInstance().getConnectionManager(context.getApplicationContext());
        if (connManager.isInternetConnected()) {
            Controller.getInstance().getConnectionManager().getHandler().sendEmptyMessage(ConnectionManager.INTERNET_FOUND);
        } else {
            Controller.getInstance().getConnectionManager().getHandler().sendEmptyMessage(ConnectionManager.INTERNET_LOST);
        }
    }
}

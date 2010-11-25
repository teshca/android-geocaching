package su.geocaching.android.ui.searchgeocache;

import android.content.Context;
import android.location.Location;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 18, 2010
 * @Description Activity which search geocache
 */
public interface ISearchActivity {
    public final static int STATUS_TYPE_GPS = 0;
    public final static int STATUS_TYPE_INTERNET = 1;

    /**
     * Init and run all search logic
     */
    public void runLogic();

    /**
     * @param status
     *            - show status to user
     */
    public void updateStatus(String status, int type);

    /**
     * @param location
     *            new user location called when user location is changer
     */
    public void updateLocation(Location location);

    /**
     * @param new user bearing called when user bearing is changer
     */
    public void updateBearing(int bearing);

    /**
     * @return last location obtained from GPS
     */
    public Location getLastKnownLocation();
}

package su.geocaching.android.ui.searchgeocache;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 18, 2010
 * @Description Activity which search geocache
 */
public interface ISearchActivity {

    /**
     * @return LocationManager object from activity
     */
    public LocationManager getLocationManager();

    /**
     * @return context from activity
     */
    public Context getContext();

    /**
     * Init and run all search logic
     */
    public void runLogic();

    /**
     * @param status - show status to user
     */
    public void updateStatus(String status);

    /**
     * @param location new user location
     * called when user location is changer
     */
    public void updateLocation(Location location);
    
    /**
     * @return last location obtained from GPS
     */
    public Location getLastKnownLocation();
}

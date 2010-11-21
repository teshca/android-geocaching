package su.geocaching.android.ui.searchgeocache;

import android.content.Context;
import android.location.Location;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 18, 2010
 * @Description Activity which search geocache
 */
public interface ISearchActivity {

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
     * @param new user bearing
     * called when user bearing is changer
     */
    public void updateAzimuth(int bearing);
    
    /**
     * @return last location obtained from GPS
     */
    public Location getLastKnownLocation();
}

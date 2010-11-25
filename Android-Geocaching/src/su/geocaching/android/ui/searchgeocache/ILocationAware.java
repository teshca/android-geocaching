package su.geocaching.android.ui.searchgeocache;

import android.location.Location;
import android.os.Bundle;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 *        <p>
 *        Interface of activities uses location updates
 *        </p>
 */
public interface ILocationAware {
    /**
     * @param location
     *            - new location
     */
    public void updateLocation(Location location);

    public void onStatusChanged(String provider, int status, Bundle extras);

    public void onProviderEnabled(String provider);

    public void onProviderDisabled(String provider);
}

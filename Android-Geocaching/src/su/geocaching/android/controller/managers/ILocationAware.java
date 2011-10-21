package su.geocaching.android.controller.managers;

import android.location.Location;
import android.os.Bundle;

/**
 * Interface of activities uses location updates
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 */
public interface ILocationAware {
    /**
     * @param location - new location
     */
    public void updateLocation(Location location);

    public void onStatusChanged(String provider, int status, Bundle extras);
}

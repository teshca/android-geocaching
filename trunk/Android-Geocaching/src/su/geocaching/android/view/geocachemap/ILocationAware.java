package su.geocaching.android.view.geocachemap;

import android.location.Location;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 * @description Interface of activities uses location updates
 */
public interface ILocationAware {
    /**
     * @param location
     *            - new location
     */
    public void updateLocation(Location location);
}

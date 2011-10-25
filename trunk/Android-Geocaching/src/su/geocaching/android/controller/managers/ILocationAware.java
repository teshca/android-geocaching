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
     * Called when location updated
     *
     * @param location - new location
     */
    public void updateLocation(Location location);

    /**
     * Called when status of location provider has been changed.
     * @param provider the name of the location provider associated with this update
     * @param status one of
     *                 <ul><li>{@link UserLocationManager#GPS_EVENT_FIRST_FIX}
     *                 <li>{@link UserLocationManager#GPS_EVENT_SATELLITE_STATUS}
     *                 <li>{@link UserLocationManager#GPS_EVENT_STARTED}
     *                 <li>{@link UserLocationManager#GPS_EVENT_STOPPED}
     *                 <li>{@link UserLocationManager#OUT_OF_SERVICE}
     *                 <li>{@link UserLocationManager#TEMPORARILY_UNAVAILABLE}</ul>
     *                 <li>{@link UserLocationManager#EVENT_PROVIDER_DISABLED}</ul>
     *                 <li>{@link UserLocationManager#EVENT_PROVIDER_ENABLED}</ul>
     * @param extras an optional Bundle which will contain provider specific status variables
     * (from {@link android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)})
     */
    public void onStatusChanged(String provider, int status, Bundle extras);
}

package su.geocaching.android.controller.managers;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This class listen status of gps engine
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 18, 2010
 */
public class GpsStatusManager implements GpsStatus.Listener {
    private final static String TAG = GpsStatusManager.class.getCanonicalName();

    private List<IGpsStatusAware> subscribers;
    private LocationManager locationManager;

    /**
     * @param locationManager manager which can add or remove updates of gps status
     */
    public GpsStatusManager(LocationManager locationManager) {
        this.locationManager = locationManager;
        subscribers = new ArrayList<IGpsStatusAware>();
        LogManager.d(TAG, "Init");
    }

    /**
     * @param subscriber activity which will be listen location updates
     */
    public void addSubscriber(IGpsStatusAware subscriber) {
        if (subscribers.size() == 0) {
            addUpdates();
        }
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
        LogManager.d(TAG, "add subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
    }

    /**
     * @param subscriber activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public boolean removeSubscriber(IGpsStatusAware subscriber) {
        boolean res = subscribers.remove(subscriber);
        if (subscribers.size() == 0) {
            removeUpdates();
        }
        LogManager.d(TAG, "remove subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
        return res;
    }

    /**
     * Add this to listeners of gps status
     */
    private void addUpdates() {
        locationManager.addGpsStatusListener(this);
        LogManager.d(TAG, "add updates");
    }

    /**
     * Remove this to listeners of gps status
     */
    private void removeUpdates() {
        locationManager.removeGpsStatusListener(this);
        LogManager.d(TAG, "remove updates");
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.GpsStatus.Listener#onGpsStatusChanged(int)
     */
    @Override
    public void onGpsStatusChanged(int arg0) {
        String status = "";
        LogManager.d(TAG, "gps status changed");
        switch (arg0) {
            case GpsStatus.GPS_EVENT_STARTED:
                status = Controller.getInstance().getResourceManager().getString(R.string.gps_status_started);
                LogManager.d(TAG, "     started");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                status = Controller.getInstance().getResourceManager().getString(R.string.gps_status_stopped);
                LogManager.d(TAG, "     stopped");
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                status = Controller.getInstance().getResourceManager().getString(R.string.gps_status_first_fix);
                LogManager.d(TAG, "     first fix");
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                status = Controller.getInstance().getResourceManager().getString(R.string.gps_status_satellite_status) + " ";
                GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                int usedInFix = 0;
                int count = 0;
                if (gpsStatus.getSatellites() == null) {
                    status = "GPS: unknown";
                    LogManager.d(TAG, "     no satellites");
                    break;
                }
                for (GpsSatellite satellite : gpsStatus.getSatellites()) {
                    count++;
                    if (satellite.usedInFix()) {
                        usedInFix++;
                    }
                }
                status += usedInFix + "/" + count;
                LogManager.d(TAG, "     satellites all=" + count + " used in fix =" + usedInFix);

        }
        for (IGpsStatusAware subscriber : subscribers) {
            subscriber.updateStatus(status);
        }
    }

}

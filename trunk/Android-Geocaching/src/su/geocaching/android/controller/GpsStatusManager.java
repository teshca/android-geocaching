package su.geocaching.android.controller;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
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

    private List<IGpsStatusAware> subsribers;
    private LocationManager locationMaganer;

    /**
     * @param locationManager manager which can add or remove updates of gps status
     */
    public GpsStatusManager(LocationManager locationManager) {
        this.locationMaganer = locationManager;
        subsribers = new ArrayList<IGpsStatusAware>();
        LogManager.d(TAG, "Init");
    }

    /**
     * @param subsriber activity which will be listen location updates
     */
    public void addSubscriber(IGpsStatusAware subsriber) {
        if (subsribers.size() == 0) {
            addUpdates();
        }
        if (!subsribers.contains(subsriber)) {
            subsribers.add(subsriber);
        }
        LogManager.d(TAG, "add subsriber. Count of subsribers became " + Integer.toString(subsribers.size()));
    }

    /**
     * @param subsriber activity which no need to listen location updates
     * @return true if activity was subsribed on location updates
     */
    public boolean removeSubsriber(IGpsStatusAware subsriber) {
        boolean res = subsribers.remove(subsriber);
        if (subsribers.size() == 0) {
            removeUpdates();
        }
        LogManager.d(TAG, "remove subsriber. Count of subsribers became " + Integer.toString(subsribers.size()));
        return res;
    }

    /**
     * Add this to listeners of gps status
     */
    private void addUpdates() {
        locationMaganer.addGpsStatusListener(this);
        LogManager.d(TAG, "add updates");
    }

    /**
     * Remove this to listeners of gps status
     */
    private void removeUpdates() {
        locationMaganer.removeGpsStatusListener(this);
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
                LogManager.d(TAG, "     stoped");
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                status = Controller.getInstance().getResourceManager().getString(R.string.gps_status_first_fix);
                LogManager.d(TAG, "     first fix");
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                status = Controller.getInstance().getResourceManager().getString(R.string.gps_status_satellite_status) + " ";
                GpsStatus gpsStatus = locationMaganer.getGpsStatus(null);
                int usedInFix = 0;
                int count = 0;
                if (gpsStatus.getSatellites() == null) {
                    status = "GPS: unknown";
                    LogManager.d(TAG, "     no satellities");
                    break;
                }
                for (GpsSatellite satellite : gpsStatus.getSatellites()) {
                    count++;
                    if (satellite.usedInFix()) {
                        usedInFix++;
                    }
                }
                status += usedInFix + "/" + count;
                LogManager.d(TAG, "     satellities all=" + count + " used in fix =" + usedInFix);

        }
        for (IGpsStatusAware subsriber : subsribers) {
            subsriber.updateStatus(status);
        }
    }

}

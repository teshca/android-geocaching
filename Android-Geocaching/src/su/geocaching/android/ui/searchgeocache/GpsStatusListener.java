package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.ui.R;
import android.location.*;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 18, 2010
 * @description This class listen status of gps engine
 */
public class GpsStatusListener implements GpsStatus.Listener {
    private ISearchActivity activity;
    
    /**
     * @param activity - activity which used this listener
     */
    public GpsStatusListener(ISearchActivity activity) {
	this.activity = activity;
    }
    
    /**
     * Called when activity resuming
     */
    public void resume() {
	activity.getLocationManager().addGpsStatusListener(this);
    }
    
    /**
     * Called when activity pausing
     */
    public void pause() {
	activity.getLocationManager().removeGpsStatusListener(this);
    }

    /* (non-Javadoc)
     * @see android.location.GpsStatus.Listener#onGpsStatusChanged(int)
     */
    @Override
    public void onGpsStatusChanged(int arg0) {
	String status = "";
	switch (arg0) {
	case GpsStatus.GPS_EVENT_STARTED:
	    status = activity.getContext().getString(R.string.gps_status_started);
	case GpsStatus.GPS_EVENT_STOPPED:
	    status = activity.getContext().getString(R.string.gps_status_stopped);
	case GpsStatus.GPS_EVENT_FIRST_FIX:
	    status = activity.getContext().getString(R.string.gps_status_first_fix);
	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	    status = activity.getContext().getString(R.string.gps_status_satellite_status)+" ";
	    GpsStatus gpsStatus = activity.getLocationManager().getGpsStatus(null);
	    int usedInFix = 0;
	    for (GpsSatellite satellite: gpsStatus.getSatellites()) {
		if (satellite.usedInFix()) {
		    usedInFix++;
		}
	    }
	    status+=usedInFix+"/"+gpsStatus.getMaxSatellites();
	}
	activity.updateStatus(status);
    }

}

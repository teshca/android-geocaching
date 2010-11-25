package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.ui.R;
import android.app.Activity;
import android.location.*;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 18, 2010
 *      <p>
 *      This class listen status of gps engine
 *      </p>
 */
public class GpsStatusListener implements GpsStatus.Listener {
    private ISearchActivity activity;
    private Activity context;
    private LocationManager locationMaganer;

    /**
     * @param activity
     *            activity which used this listener
     */
    public GpsStatusListener(ISearchActivity activity) {
	this.activity = activity;
	context = (Activity) activity;
	locationMaganer = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
    }

    /**
     * Called when activity resuming
     */
    public void resume() {
	locationMaganer.addGpsStatusListener(this);
    }

    /**
     * Called when activity pausing
     */
    public void pause() {
	locationMaganer.removeGpsStatusListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.GpsStatus.Listener#onGpsStatusChanged(int)
     */
    @Override
    public void onGpsStatusChanged(int arg0) {
	String status = "";
	switch (arg0) {
	case GpsStatus.GPS_EVENT_STARTED:
	    status = context.getString(R.string.gps_status_started);
	case GpsStatus.GPS_EVENT_STOPPED:
	    status = context.getString(R.string.gps_status_stopped);
	case GpsStatus.GPS_EVENT_FIRST_FIX:
	    status = context.getString(R.string.gps_status_first_fix);
	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	    status = context.getString(R.string.gps_status_satellite_status) + " ";
	    GpsStatus gpsStatus = locationMaganer.getGpsStatus(null);
	    int usedInFix = 0;
	    int count = 0;
	    if (gpsStatus.getSatellites() == null) {
		status = "GPS: unknown";
		break;
	    }
	    for (GpsSatellite satellite : gpsStatus.getSatellites()) {
		count++;
		if (satellite.usedInFix()) {
		    usedInFix++;
		}
	    }
	    status += usedInFix + "/" + count;
	}
	activity.updateStatus(status, ISearchActivity.STATUS_TYPE_GPS);
    }

}

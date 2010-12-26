package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.utils.Helper;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 20, 2010F
 */
public class UserLocationOverlay extends MyLocationOverlay {
    private static final int COMPASS_POINT_X = 50;
    private static final int COMPASS_POINT_Y = 60;
    private static final int FINGER_WIDTH = 25;
    private static final int FINGER_HEIGHT = 25;

    private MapView map;
    private SearchGeoCacheMap context;

    /**
     * @param arg0
     *            context which implements ISearchActivity
     * @param arg1
     *            map which will be contain this overlay
     */
    public UserLocationOverlay(SearchGeoCacheMap arg0, MapView arg1) {
	super((Context) arg0, arg1);
	context = arg0;
	map = arg1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.android.maps.MyLocationOverlay#drawMyLocation(android.graphics
     * .Canvas, com.google.android.maps.MapView, android.location.Location,
     * com.google.android.maps.GeoPoint, long)
     */
    @Override
    protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
	if (context.getLastKnownLocation() != null) {
	    super.drawMyLocation(canvas, map, context.getLastKnownLocation(), Helper.locationToGeoPoint(context.getLastKnownLocation()), when);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.android.maps.MyLocationOverlay#drawCompass(android.graphics
     * .Canvas, float)
     */
    protected void drawCompass(Canvas canvas, float bearing) {
	super.drawCompass(canvas, context.getLastKnownBearing());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.android.maps.MyLocationOverlay#onTap(com.google.android.maps
     * .GeoPoint, com.google.android.maps.MapView)
     */
    @Override
    public boolean onTap(GeoPoint p, MapView map) {
	if ((context.getLastKnownLocation() == null) || (!this.isCompassEnabled())) {
	    return super.onTap(p, map);
	}
	Projection proj = map.getProjection();
	Point compassPoint = new Point(COMPASS_POINT_X, COMPASS_POINT_Y);
	Point tapPoint = new Point();
	proj.toPixels(p, tapPoint);
	if ((Math.abs(compassPoint.x - tapPoint.x) < FINGER_WIDTH) && (Math.abs(compassPoint.y - tapPoint.y) < FINGER_HEIGHT)) {
	    context.startCompassView();
	    return true;
	}
	return false;
    }
}

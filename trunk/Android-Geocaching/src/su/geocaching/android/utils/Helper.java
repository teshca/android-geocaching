package su.geocaching.android.utils;

import java.text.DecimalFormat;

import android.location.Location;

import com.google.android.maps.GeoPoint;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 12, 2010
 *      <p>
 *      This class is subset of common method, which we often use
 *      </p>
 */
public class Helper {
    // if distance(m)
    // greater than this
    // show (x/1000) km else x m
    private static final int BIG_DISTANCE_VALUE = 10000;

    private static final DecimalFormat BIG_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat SMALL_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0");
    private static final String BIG_DISTANCE_VALUE_NAME = "κμ";
    private static final String SMALL_DISTANCE_VALUE_NAME = "μ";
    private static final float BIG_DISTANCE_COEFFICIENT = (float) 0.001;
    private static final float SMALL_DISTANCE_COEFFICIENT = 1;

    /**
     * @param l1
     *            first location
     * @param l2
     *            second location
     * @return distance between locations in meters
     */
    public static float getDistanceBetween(Location l1, Location l2) {
	float[] results = new float[3];
	Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), results);
	return results[0];
    }

    /**
     * @param l1
     *            location
     * @param l2
     *            GeoPoint
     * @return distance between locations in meters
     */
    public static float getDistanceBetween(Location l1, GeoPoint l2) {
	float[] results = new float[3];
	Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitudeE6() / 1E6, l2.getLongitudeE6() / 1E6, results);
	return results[0];
    }

    /**
     * @param l1
     *            GeoPoint
     * @param l2
     *            location
     * @return distance between locations in meters
     */
    public static float getDistanceBetween(GeoPoint l1, Location l2) {
	return getDistanceBetween(l2, l1);
    }

    /**
     * @param l1
     *            first GeoPoint
     * @param l2
     *            second GeoPoint
     * @return distance between locations in meters
     */
    public static float getDistanceBetween(GeoPoint l1, GeoPoint l2) {
	float[] results = new float[3];
	Location.distanceBetween(l1.getLatitudeE6() / 1E6, l1.getLongitudeE6() / 1E6, l2.getLatitudeE6() / 1E6, l2.getLongitudeE6() / 1E6, results);
	return results[0];
    }

    /**
     * @param l1
     *            location from
     * @param l2
     *            location to
     * @return bearing of direction from l1 to l2 in degrees
     */
    public static float getBearingBetween(Location l1, GeoPoint l2) {
	float[] results = new float[3];
	Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitudeE6() / 1E6, l2.getLongitudeE6() / 1E6, results);
	return results[1];
    }

    /**
     * @param dist
     *            distance (suggested to geocache in meters)
     * @return String of distance formatted value and measure
     */
    public static String distanceToString(float dist) {
	String textDistance = "";
	if (dist >= BIG_DISTANCE_VALUE) {
	    textDistance = BIG_DISTANCE_NUMBER_FORMAT.format(dist * BIG_DISTANCE_COEFFICIENT) + " " + BIG_DISTANCE_VALUE_NAME;
	} else {
	    textDistance = SMALL_DISTANCE_NUMBER_FORMAT.format(dist * SMALL_DISTANCE_COEFFICIENT) + " " + SMALL_DISTANCE_VALUE_NAME;
	}
	return textDistance;
    }

    /**
     * @param location
     *            - Location object
     * @return location coverted to GeoPoint object
     */
    public static GeoPoint locationToGeoPoint(Location location) {
	return new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
    }
}

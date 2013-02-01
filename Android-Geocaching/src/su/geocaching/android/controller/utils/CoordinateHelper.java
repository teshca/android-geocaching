package su.geocaching.android.controller.utils;

import android.content.res.Resources;
import android.location.Location;
import su.geocaching.android.model.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * This class is subset of common method, which we often use
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 12, 2010
 */
public class CoordinateHelper {
    // if distance(m)
    // greater than this
    // show (x/1000) km else x m
    private static final int BIG_DISTANCE_VALUE = 1000; // distance in meters which mean "big distance"
    private static final int EARTH_RADIUS = 6371000;

    private static final String SMALL_PRECISE_DISTANCE_NUMBER_FORMAT = Controller.getInstance().getResourceManager().getString(R.string.small_distance_precise_format);
    private static final String SMALL_NOT_PRECISE_DISTANCE_NUMBER_FORMAT = Controller.getInstance().getResourceManager().getString(R.string.small_distance_not_precise_format);
    private static final String BIG_PRECISE_DISTANCE_NUMBER_FORMAT = Controller.getInstance().getResourceManager().getString(R.string.big_distance_precise_format);
    private static final String BIG_NOT_PRECISE_DISTANCE_NUMBER_FORMAT = Controller.getInstance().getResourceManager().getString(R.string.big_distance_not_precise_format);

    private static final String BIG_DISTANCE_VALUE_NAME = Controller.getInstance().getResourceManager().getString(R.string.kilometer);
    private static final String SMALL_DISTANCE_VALUE_NAME = Controller.getInstance().getResourceManager().getString(R.string.meter);
    private static final float BIG_DISTANCE_COEFFICIENT = 0.001f; // how many small_distance_name units in big_distance_units
    private static final float SMALL_DISTANCE_COEFFICIENT = 1f;

    /**
     * @param l1
     *            first location
     * @param l2
     *            second location
     * @return distance between locations in meters
     */
    public static float getDistanceBetween(Location l1, Location l2) {
        float[] results = new float[1];
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
        float[] results = new float[1];
        Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), results);
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
        float[] results = new float[1];
        Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), results);
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
        float[] results = new float[2];
        Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), results);
        return results[1];
    }

    public static float getBearingBetween(GeoPoint l1, GeoPoint l2) {
        float[] results = new float[2];
        Location.distanceBetween(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude(), results);
        return results[1];
    }

    /**
     * @param dist distance (suggested to geocache in meters)
     * @return String of distance formatted value and measure
     */
    public static String distanceToString(float dist) {
        return distanceToString(dist, true);
    }

    /**
     * @param dist      distance (suggested to geocache in meters)
     * @param isPrecise true, if distance value precise
     * @return String of distance formatted value and measure
     */
    public static String distanceToString(float dist, boolean isPrecise) {
        String textDistance;
        if (isPrecise) {
            if (dist >= BIG_DISTANCE_VALUE) {
                textDistance = String.format(BIG_PRECISE_DISTANCE_NUMBER_FORMAT, dist * BIG_DISTANCE_COEFFICIENT, BIG_DISTANCE_VALUE_NAME);
            } else {
                textDistance = String.format(SMALL_PRECISE_DISTANCE_NUMBER_FORMAT, dist * SMALL_DISTANCE_COEFFICIENT, SMALL_DISTANCE_VALUE_NAME);
            }
        } else {
            if (dist >= BIG_DISTANCE_VALUE) {
                textDistance = String.format(BIG_NOT_PRECISE_DISTANCE_NUMBER_FORMAT, dist * BIG_DISTANCE_COEFFICIENT, BIG_DISTANCE_VALUE_NAME);
            } else {
                textDistance = String.format(SMALL_NOT_PRECISE_DISTANCE_NUMBER_FORMAT, dist * SMALL_DISTANCE_COEFFICIENT, SMALL_DISTANCE_VALUE_NAME);
            }
        }
        return textDistance;
    }

    /**
     * @param location
     *            - Location object
     * @return location coverted to GeoPoint object
     */
    public static GeoPoint locationToGeoPoint(Location location) {
        return new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    /**
     * Formatting coordinate in accordance with standard
     *
     * @param location
     *            - coordinates
     * @return formating string (for example: "60° 12,123' с.ш. | 30° 32,321'" в.д.)
     */
    public static String coordinateToString(GeoPoint location) {
        Sexagesimal latitude = new Sexagesimal(location.getLatitude()).roundTo(3);
        Sexagesimal longitude = new Sexagesimal(location.getLongitude()).roundTo(3);

        Resources res = Controller.getInstance().getResourceManager().getResources();
        String format;

        if (latitude.degrees > 0) {
            if (longitude.degrees > 0) {
                format = res.getString(R.string.ne_template);
            } else {
                format = res.getString(R.string.nw_template);
            }
        } else {
            if (longitude.degrees > 0) {
                format = res.getString(R.string.se_template);
            } else {
                format = res.getString(R.string.sw_template);
            }
        }
        return String.format(format, latitude.degrees, latitude.minutes, longitude.degrees, longitude.minutes);
    }

  /**
   * Calculate geopoint that located at a distance "distance" in the "bearing" direction from currentGeoPoint
   *
   * @param currentGeoPoint current location
   * @param bearing direction to the goal point
   * @param distance distance to the goal point
   * @return goal geopoint
   */
    public static GeoPoint distanceBearingToGeoPoint(GeoPoint currentGeoPoint, float bearing, float distance) {
        double latitude = Math.toRadians(currentGeoPoint.getLatitude());
        double longitude = Math.toRadians(currentGeoPoint.getLongitude());
        double radianBearing = Math.toRadians(bearing);

        double distanceDivRadius = distance / EARTH_RADIUS;

        // Calculating goal Location
        double goalLatitude = Math.asin(Math.sin(latitude) * Math.cos(distanceDivRadius) + Math.cos(latitude) * Math.sin(distanceDivRadius) * Math.cos(radianBearing));
        double goalLongitude = longitude
                + Math.atan2(Math.sin(radianBearing) * Math.sin(distanceDivRadius) * Math.cos(latitude), Math.cos(distanceDivRadius) - Math.sin(latitude) * Math.sin(goalLatitude));

        goalLatitude = Math.toDegrees(goalLatitude);
        goalLongitude = Math.toDegrees(goalLongitude);
        return new GeoPoint(goalLatitude, goalLongitude);
    }
}
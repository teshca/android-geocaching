package su.geocaching.android.utils;

import android.content.res.Resources;
import android.location.Location;
import com.google.android.maps.GeoPoint;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.ui.R;

import java.text.DecimalFormat;

/**
 * This class is subset of common method, which we often use
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 12, 2010
 */
public class GpsHelper {
    // if distance(m)
    // greater than this
    // show (x/1000) km else x m
    private static final int BIG_DISTANCE_VALUE = 10000;
    private static final int EARTH_RADIUS = 6371000;

    private static final DecimalFormat BIG_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat SMALL_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0");
    private static final String BIG_DISTANCE_VALUE_NAME = Controller.getInstance().getResourceManager().getString(R.string.kilometer);
    private static final String SMALL_DISTANCE_VALUE_NAME = Controller.getInstance().getResourceManager().getString(R.string.meter);;
    private static final float BIG_DISTANCE_COEFFICIENT = 0.001f;
    private static final float SMALL_DISTANCE_COEFFICIENT = 1f;

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
        String textDistance;
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

    // public static int[] decimalToSexagesimal(double coordinate) throws Exception {
    // if (Math.abs(coordinate) > 180) {
    // throw new Exception("invalid value");
    // }
    //
    // int[] sexagesimal = new int[3];
    // sexagesimal[0] = (int) coordinate;
    // coordinate -= sexagesimal[0];
    // coordinate = Math.abs(coordinate * 60);
    // sexagesimal[1] = (int) coordinate;
    // coordinate -= sexagesimal[1];
    // coordinate *= 1000;
    // sexagesimal[2] = (int) coordinate;
    // return sexagesimal;
    // }

    public static int[] coordinateE6ToSexagesimal(int coordinate) {

        int[] sexagesimal = new int[3];
        sexagesimal[0] = coordinate / 1000000;
        coordinate %= 1000000;
        coordinate = Math.abs(coordinate * 6 / 10);
        sexagesimal[1] = coordinate / 10000;
        coordinate %= 10000;
        sexagesimal[2] = (int) Math.round((double) coordinate / 10);
        return sexagesimal;
    }

    public static int sexagesimalToCoordinateE6(int degrees, int minutes, int mMinutes) throws Exception {
        if (Math.abs(degrees) > 180 || minutes >= 60) {
            throw new Exception("Invalid data format");
        }
        int coordinateE6 = (int) (degrees * 1E6);
        coordinateE6 += (minutes * 1E3 + mMinutes) * 100 / 6;
        return coordinateE6;
    }

    /**
     * Formatting coordinate in accordance with standard
     * 
     * @param location
     *            - coordinates
     * @return formating string (for example: "60° 12,123' ñ.ø. | 30° 32,321'" â.ä.)
     */
    public static String coordinateToString(GeoPoint location) {
        int[] latitude = coordinateE6ToSexagesimal(location.getLatitudeE6());
        int[] longitude = coordinateE6ToSexagesimal(location.getLatitudeE6());

        Resources res = Controller.getInstance().getResourceManager().getResources();
        String format;

        if (latitude[0] > 0) {
            if (longitude[0] > 0) {
                format = res.getString(R.string.ne_template);
            } else {
                format = res.getString(R.string.nw_template);
            }
        } else {
            if (longitude[0] > 0) {
                format = res.getString(R.string.se_template);
            } else {
                format = res.getString(R.string.sw_template);
            }
        }
        return String.format(format, latitude[0], latitude[1], latitude[2], longitude[0], longitude[1], longitude[2]);

    }

    // TODO still not work
    public static GeoPoint distanceBearingToGeoPoint(GeoPoint currentGeoPoint, int bearing, int distance) {
        double latitude = currentGeoPoint.getLatitudeE6() / 1E6;
        double longitude = currentGeoPoint.getLatitudeE6() / 1E6;
        double radianBearing = bearing * Math.PI / 180;
        LogManager.d("Geocaching.su", "radianBearing = " + radianBearing);
        double distanceDivRadius = (double) distance / EARTH_RADIUS;

        // Calculating goal Location
        double goalLatitude = Math.asin(Math.sin(latitude) * Math.cos(distanceDivRadius) + Math.cos(latitude) * Math.sin(distanceDivRadius) * Math.cos(radianBearing));
        double goalLonitude = longitude
                + Math.atan2(Math.sin(radianBearing) * Math.sin(distanceDivRadius) * Math.cos(latitude), Math.cos(distanceDivRadius) - Math.sin(latitude) * Math.sin(goalLatitude));

        return new GeoPoint((int) (goalLatitude * 1E6), (int) (goalLonitude * 1E6));
    }
}

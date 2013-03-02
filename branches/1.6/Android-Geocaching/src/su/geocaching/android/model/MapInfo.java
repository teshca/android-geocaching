package su.geocaching.android.model;

import java.io.Serializable;

/**
 * Represent information about map center and map zoom
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class MapInfo implements Serializable {
    public static final double DEFAULT_CENTER_LONGITUDE = 29.828674;
    public static final double DEFAULT_CENTER_LATITUDE = 59.879904;
    public static final float DEFAULT_ZOOM = 13;

    private double centerX;
    private double centerY;
    private float zoom;

    /**
     * Init all settings by default values
     */
    public MapInfo() {
        centerX = DEFAULT_CENTER_LATITUDE;
        centerY = DEFAULT_CENTER_LONGITUDE;
        zoom = DEFAULT_ZOOM;
    }

    /**
     * @param centerX
     *         latitude of map center point
     * @param centerY
     *         logitude of map center point
     * @param zoom
     *         map zoom value
     */
    public MapInfo(double centerX, double centerY, float zoom) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
    }

    /**
     * @return latitude of map center point
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * @return logitude of map center point
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     * @return map zoom value
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * @param centerX
     *         latitude of map center point
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    /**
     * @param centerY
     *         longitude of map center point
     */
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    /**
     * @param zoom
     *         map zoom value
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
}

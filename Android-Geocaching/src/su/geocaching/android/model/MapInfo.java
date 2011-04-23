package su.geocaching.android.model;

/**
 * Represent information about map center and map zoom
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class MapInfo {
    public static final int DEFAULT_CENTER_LONGITUDE = 29828674;
    public static final int DEFAULT_CENTER_LATITUDE = 59879904;
    public static final int DEFAULT_ZOOM = 13;

    private int centerX;
    private int centerY;
    private int zoom;
    private int geoCacheId;

    /**
     * Init all settings by default values
     */
    public MapInfo() {
        centerX = DEFAULT_CENTER_LATITUDE;
        centerY = DEFAULT_CENTER_LONGITUDE;
        zoom = DEFAULT_ZOOM;
        geoCacheId = -1;
    }

    /**
     * @param centerX
     *            latitude of map center point
     * @param centerY
     *            logitude of map center point
     * @param zoom
     *            map zoom value
     */
    public MapInfo(int centerX, int centerY, int zoom) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        geoCacheId = -1;
    }

    /**
     * @param centerX
     *            latitude of map center point
     * @param centerY
     *            logitude of map center point
     * @param zoom
     *            map zoom value
     * @param geoCacheId
     *            id of GeoCache
     */
    public MapInfo(int centerX, int centerY, int zoom, int geoCacheId) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.geoCacheId = geoCacheId;
    }

    /**
     * @return latitude of map center point
     */
    public int getCenterX() {
        return centerX;
    }

    /**
     * @return logitude of map center point
     */
    public int getCenterY() {
        return centerY;
    }

    /**
     * @return map zoom value
     */
    public int getZoom() {
        return zoom;
    }

    /**
     * @return geocache id
     */
    public int getGeoCacheId() {
        return geoCacheId;
    }

    /**
     * @param centerX
     *            latitude of map center point
     */
    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    /**
     * @param centerY
     *            longitude of map center point
     */
    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    /**
     * @param zoom
     *            map zoom value
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    /**
     * @param geoCacheId
     *            id of geocache
     */
    public void setGeoCacheId(int geoCacheId) {
        this.geoCacheId = geoCacheId;
    }
}

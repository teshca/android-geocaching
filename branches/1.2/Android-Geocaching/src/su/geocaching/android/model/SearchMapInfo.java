package su.geocaching.android.model;

import java.io.Serializable;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since April 2011
 * 
 */
public class SearchMapInfo extends MapInfo implements Serializable{
    private int geoCacheId;

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
    public SearchMapInfo(int centerX, int centerY, int zoom, int geoCacheId) {
        super(centerX, centerY, zoom);
        this.geoCacheId = geoCacheId;
    }

    /**
     * @return the geoCacheId
     */
    public int getGeoCacheId() {
        return geoCacheId;
    }

    /**
     * @param geoCacheId
     *            the geoCacheId to set
     */
    public void setGeoCacheId(int geoCacheId) {
        this.geoCacheId = geoCacheId;
    }
}

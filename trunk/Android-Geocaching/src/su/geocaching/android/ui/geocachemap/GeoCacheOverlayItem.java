package su.geocaching.android.ui.geocachemap;

import android.content.Context;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;

import java.util.List;

/**
 * Describes overlay item which contains GeoCache object
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 20, 2010
 */
public class GeoCacheOverlayItem extends OverlayItem {

    private GeoCache mData;
    private List<GeoCache> geoCacheList = null;

    /**
     * @param geoCache which will be kept in this overlay
     * @param title    from default constructor of OverlayItem
     * @param snippet  from default constructor of OverlayItem
     */
    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet) {
        super(geoCache.getLocationGeoPoint(), title, snippet);
        mData = geoCache;
    }

    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet, Context map) {
        super(geoCache.getLocationGeoPoint(), title, snippet);
        this.setMarker(Controller.getInstance().getMarker(geoCache, map));
        mData = geoCache;
    }

    public GeoCacheOverlayItem(GeoPoint point, List<GeoCache> geoCacheList, String title, String snippet, Context map) {
        super(point, title, snippet);
        mData = new GeoCache();
        mData.setType(GeoCacheType.GROUP);
        this.setMarker(Controller.getInstance().getMarker(mData, map));
        mData.setLocationGeoPoint(point);
        this.geoCacheList = geoCacheList;
    }

    /**
     * @return GeoCache object which contains by this overlay
     *         use if it's not a group
     */
    public GeoCache getGeoCache() {
        return mData;
    }

    public List<GeoCache> getGeoCacheList() {
        return geoCacheList;
    }

}

package su.geocaching.android.ui.geocachemap;

import android.content.Context;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;

/**
 * Describes overlay item which contains GeoCache object
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 20, 2010
 */
public class GeoCacheOverlayItem extends OverlayItem {

    private GeoCache geoCache;

    /**
     * @param geoCache which will be kept in this overlay
     * @param title    from default constructor of OverlayItem
     * @param snippet  from default constructor of OverlayItem
     */
    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet) {
        super(geoCache.getLocationGeoPoint(), title, snippet);
        this.geoCache = geoCache;
    }

    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet, Context map) {
        super(geoCache.getLocationGeoPoint(), title, snippet);
        this.setMarker(Controller.getInstance().getResourceManager().getMarker(geoCache));
        this.geoCache = geoCache;
    }

    public GeoCacheOverlayItem(GeoPoint point, String title, String snippet, Context map) {
        super(point, title, snippet);
        geoCache = new GeoCache();
        geoCache.setType(GeoCacheType.GROUP);
        this.setMarker(Controller.getInstance().getResourceManager().getMarker(geoCache));
        geoCache.setLocationGeoPoint(point);
    }

    /**
     * @return GeoCache object which contains by this overlay use if it's not a group
     */
    public GeoCache getGeoCache() {
        return geoCache;
    }
}

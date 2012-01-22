package su.geocaching.android.ui.geocachemap;

import android.graphics.drawable.Drawable;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Describes overlay item which contains GeoCache object
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 20, 2010
 */
public class GeoCacheOverlayItem extends OverlayItem {

    private GeoCache geoCache;

    /**
     * @param geoCache
     *         which will be kept in this overlay
     * @param title
     *         from default constructor of OverlayItem
     * @param snippet
     *         from default constructor of OverlayItem
     */
    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet) {
        super(geoCache.getLocationGeoPoint(), title, snippet);
        this.geoCache = geoCache;
    }

    public GeoCacheOverlayItem(GeoPoint point, String title, String snippet) {
        super(point, title, snippet);
        geoCache = new GeoCache();
        geoCache.setType(GeoCacheType.GROUP);
        geoCache.setLocationGeoPoint(point);
    }

    public Drawable getMarker(int i) {
        return Controller.getInstance().getResourceManager().getCacheMarker(geoCache.getType(), geoCache.getStatus());
    }

    /**
     * @return GeoCache object which contains by this overlay use if it's not a group
     */
    public GeoCache getGeoCache() {
        return geoCache;
    }
}

package su.geocaching.android.ui.geocachemap;

import su.geocaching.android.model.datatype.GeoCache;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 20, 2010
 * @Description describes overlay item which contains GeoCache object
 */
public class GeoCacheOverlayItem extends OverlayItem {

    private GeoCache mData;

    /**
     * @param geoCache
     *            which will be kept in this overlay
     * @param title
     *            from default constructor of OverlayItem
     * @param snippet
     *            from default constructor of OverlayItem
     */
    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet) {
	super(geoCache.getLocationGeoPoint(), title, snippet);
	mData = geoCache;
    }

    /**
     * @return GeoCache object which contains by this overlay
     */
    public GeoCache getGeoCache() {
	return mData;
    }

}

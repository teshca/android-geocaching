package su.geocaching.android.ui.geocachemap;

import android.content.Context;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;

import com.google.android.maps.OverlayItem;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 20, 2010
 *      <p>
 *      Describes overlay item which contains GeoCache object
 *      </p>
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

    public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet, Context map) {
	super(geoCache.getLocationGeoPoint(), title, snippet);
	this.setMarker(Controller.getInstance().getMarker(geoCache, map));
	mData = geoCache;
    }

    /**
     * @return GeoCache object which contains by this overlay
     */
    public GeoCache getGeoCache() {
	return mData;
    }

}

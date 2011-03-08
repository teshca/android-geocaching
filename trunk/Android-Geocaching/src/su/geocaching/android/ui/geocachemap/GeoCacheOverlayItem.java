package su.geocaching.android.ui.geocachemap;

import java.util.List;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;
import android.content.Context;

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
	private List<GeoCache> geoCacheList = null;

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
		this.geoCache = geoCache;
	}

	public GeoCacheOverlayItem(GeoCache geoCache, String title, String snippet, Context map) {
		super(geoCache.getLocationGeoPoint(), title, snippet);
		this.setMarker(Controller.getInstance().getMarker(geoCache));
		this.geoCache = geoCache;
	}

	public GeoCacheOverlayItem(GeoPoint point, List<GeoCache> geoCacheList, String title, String snippet, Context map) {
		super(point, title, snippet);
		geoCache = new GeoCache();
		geoCache.setType(GeoCacheType.GROUP);
		this.setMarker(Controller.getInstance().getMarker(geoCache));
		geoCache.setLocationGeoPoint(point);
		this.geoCacheList = geoCacheList;
	}

	/**
	 * @return GeoCache object which contains by this overlay use if it's not a group
	 */
	public GeoCache getGeoCache() {
		return geoCache;
	}

	public List<GeoCache> getGeoCacheList() {
		return geoCacheList;
	}

}

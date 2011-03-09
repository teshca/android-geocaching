package su.geocaching.android.controller;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Manager which can get access to application resources
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Mar 8, 2011
 */
public class ResourceManager {
	private Context context;

	public ResourceManager(Context context) {
		this.context = context;
	}

	public Drawable getDrawable(int id) {
		return context.getResources().getDrawable(id);
	}

	public String getString(int id) {
		return context.getString(id);
	}

	public String getString(int id, Object... formatArgs) {
		return context.getString(id, formatArgs);
	}

	public CharSequence getText(int id) {
		return context.getText(id);
	}

	public Resources getResources() {
		return context.getResources();
	}

	/**
	 * Return marker for map of input geoCache
	 * 
	 * @param geoCache
	 *            we want to draw on the map
	 * @return Drawable for this geoCache depends on it's parameters
	 */
	public Drawable getMarker(GeoCache geoCache) {
		if (getMarkerResId(geoCache) == -1) {
			return null;
		}
		return getMarker(getMarkerResId(geoCache));
	}

	/**
	 * Return marker resource id of input geoCache
	 * 
	 * @param geoCache
	 *            we want to draw on the map
	 * @return Drawable for this geoCache depends on it's parameters
	 */
	public int getMarkerResId(GeoCache geoCache) {
		switch (geoCache.getType()) {
		case TRADITIONAL:
			switch (geoCache.getStatus()) {
			case VALID:
				return R.drawable.ic_cache_traditional_valid;
			case NOT_VALID:
				return R.drawable.ic_cache_traditional_not_valid;
			case NOT_CONFIRMED:
				return R.drawable.ic_cache_traditional_not_confirmed;
			}
			break;
		case VIRTUAL:
			switch (geoCache.getStatus()) {
			case VALID:
				return R.drawable.ic_cache_virtual_valid;
			case NOT_VALID:
				return R.drawable.ic_cache_virtual_not_valid;
			case NOT_CONFIRMED:
				return R.drawable.ic_cache_virtual_not_confirmed;
			}
			break;
		case STEP_BY_STEP:
			switch (geoCache.getStatus()) {
			case VALID:
				return R.drawable.ic_cache_stepbystep_valid;
			case NOT_VALID:
				return R.drawable.ic_cache_stepbystep_not_valid;
			case NOT_CONFIRMED:
				return R.drawable.ic_cache_stepbystep_not_confirmed;
			}
			break;
		case EXTREME:
			switch (geoCache.getStatus()) {
			case VALID:
				return R.drawable.ic_cache_extreme_valid;
			case NOT_VALID:
				return R.drawable.ic_cache_extreme_not_valid;
			case NOT_CONFIRMED:
				return R.drawable.ic_cache_extreme_not_confirmed;
			}
			break;
		case EVENT:
			switch (geoCache.getStatus()) {
			case VALID:
				return R.drawable.ic_cache_event_valid;
			case NOT_VALID:
				return R.drawable.ic_cache_event_not_valid;
			case NOT_CONFIRMED:
				return R.drawable.ic_cache_event_not_confirmed;
			}
			break;
		case GROUP:
			return R.drawable.ic_cache_group;
		case CHECKPOINT:
			return R.drawable.cache;
		}
		return -1;
	}

	/**
	 * Set bounds to marker
	 * 
	 * @param resource
	 *            id of marker
	 * @return marker with set bounds
	 */
	private Drawable getMarker(int resource) {
		Drawable cacheMarker = context.getResources().getDrawable(resource);
		cacheMarker.setBounds(-cacheMarker.getMinimumWidth() / 2, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth() / 2, 0);
		return cacheMarker;
	}

	/**
	 * @param geoCache
	 *            input geo cache
	 * @return localized name of geocache status
	 */
	public String getGeoCacheStatus(GeoCache geoCache) {
		switch (geoCache.getStatus()) {
		case VALID:
			return getString(R.string.status_geocache_valid);
		case NOT_VALID:
			return getString(R.string.status_geocache_no_valid);
		case NOT_CONFIRMED:
			return getString(R.string.status_geocache_no_confirmed);
		case ACTIVE_CHECKPOINT:
			return getString(R.string.status_geocache_active_checkpoint);
		case NOT_ACTIVE_CHECKPOINT:
			return getString(R.string.status_geocache_not_active_checkpoint);
		default:
			return getString(R.string.status_geocache_unknown);
			// what a terrible failure?
		}
	}

	/**
	 * @param geoCache
	 *            input geo cache
	 * @return localized name of geocache type
	 */
	public String getGeoCacheType(GeoCache geoCache) {
		switch (geoCache.getType()) {
		case TRADITIONAL:
			return getString(R.string.type_geocache_traditional);
		case VIRTUAL:
			return getString(R.string.type_geocache_virtua);
		case EVENT:
			return getString(R.string.type_geocache_event);
		case EXTREME:
			return getString(R.string.type_geocache_extreme);
		case STEP_BY_STEP:
			return getString(R.string.type_geocache_step_by_step);
		case CHECKPOINT:
			return getString(R.string.type_geocache_checkpoint);
		default:
			return getString(R.string.status_geocache_unknown);
			// what a terrible failure?
		}
	}
}

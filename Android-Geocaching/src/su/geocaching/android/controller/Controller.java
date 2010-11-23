package su.geocaching.android.controller;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.controller.filter.NoFilter;
import su.geocaching.android.model.datastorage.GeoCacheStorage;
import su.geocaching.android.model.datastorage.SettingsStorage;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import android.graphics.drawable.Drawable;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * Author: Yuri Denison Date: 04.11.2010 21:06:02
 */
public class Controller {
    private static Controller instance;

    private IApiManager apiManager;
    private GeoCacheStorage favoriteGeoCacheStorage;
    private SettingsStorage settingsStorage;

    private Controller() {
	apiManager = ApiManager.getInstance();
	favoriteGeoCacheStorage = GeoCacheStorage.getInstance();
	settingsStorage = SettingsStorage.getInstance();
    }

    public static Controller getInstance() {
	if (instance == null) {
	    synchronized (Controller.class) {
		if (instance == null) {
		    instance = new Controller();
		}
	    }
	}
	return instance;
    }

    /**
     * @param maxLatitude
     *            - coordinate of the visible area
     * @param minLatitude
 *            - coordinate of the visible area
     * @param maxLongitude
*            - coordinate of the visible area
     * @param minLongitude
     */
    public void updateSelectedGeoCaches(SelectGeoCacheMap map, double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
	Double[] d = { maxLatitude, minLatitude, maxLongitude, minLongitude };	
	new DownloadGeoCacheTask(map).execute(d);
    }

    /**
     * Get favorite GeoCaches filtered with chosen filters
     * 
     * @param filterList
     *            - list of filters (if null - no filter)
     * @return LinkedList<GeoCache>
     */
    public List<GeoCache> getFavoriteGeoCaches(List<IFilter> filterList) {
	if (filterList == null) {
	    return favoriteGeoCacheStorage.getGeoCacheList();
	} else {
	    List<GeoCache> list = favoriteGeoCacheStorage.getGeoCacheList();
	    for (IFilter filter : filterList) {
		list = filter.filter(list);
	    }
	    return list;
	}
    }

    /**
     * @return List of geoCaches filters from settings if
     *         settingsStorage.getFilters() returns null, than return NoFilter
     */
    public LinkedList<IFilter> getFilterList() {
	LinkedList<IFilter> list = new LinkedList<IFilter>();
	if (settingsStorage.getFilters() == null) {
	    list.add(NoFilter.getInstance());
	} else {
	    list.addAll(settingsStorage.getFilters());
	}
	return list;
    }

    /**
     * @param geoCache
     *            we want to draw on the map
     * @param map
     *            -
     * @return Drawable for this geoCache depends on it's parameters
     */
    public Drawable getMarker(GeoCache geoCache, Context map) {
	// TODO: add different icons for different types of geoCache
	switch (geoCache.getStatus()) {
	case VALID:
	    switch (geoCache.getType()) {
	    case TRADITIONAL:
		return getMarker(R.drawable.orangecache, map);
	    case VIRTUAL:
		return getMarker(R.drawable.orangecache, map);
	    case STEP_BY_STEP:
		return getMarker(R.drawable.orangecache, map);
	    case EXTREME:
		return getMarker(R.drawable.orangecache, map);
	    case EVENT:
		return getMarker(R.drawable.orangecache, map);
	    }
	    break;
	case NOT_VALID:
	    switch (geoCache.getType()) {
	    case TRADITIONAL:
		return getMarker(R.drawable.orangecache, map);
	    case VIRTUAL:
		return getMarker(R.drawable.orangecache, map);
	    case STEP_BY_STEP:
		return getMarker(R.drawable.orangecache, map);
	    case EXTREME:
		return getMarker(R.drawable.orangecache, map);
	    case EVENT:
		return getMarker(R.drawable.orangecache, map);
	    }
	    break;
	case NOT_CONFIRMED:
	    switch (geoCache.getType()) {
	    case TRADITIONAL:
		return getMarker(R.drawable.orangecache, map);
	    case VIRTUAL:
		return getMarker(R.drawable.orangecache, map);
	    case STEP_BY_STEP:
		return getMarker(R.drawable.orangecache, map);
	    case EXTREME:
		return getMarker(R.drawable.orangecache, map);
	    case EVENT:
		return getMarker(R.drawable.orangecache, map);
	    }
	    break;
	}
	return null;
    }

    private Drawable getMarker(int resource, Context map) {
	Drawable cacheMarker = map.getResources().getDrawable(resource);
	cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth(), 0);
	return cacheMarker;
    }
}
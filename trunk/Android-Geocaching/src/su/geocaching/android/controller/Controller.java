package su.geocaching.android.controller;

import android.graphics.drawable.Drawable;
import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.filter.Filter;
import su.geocaching.android.model.dataStorage.GeoCacheStorage;
import su.geocaching.android.model.dataStorage.SettingsStorage;
import su.geocaching.android.model.dataType.GeoCache;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geoCacheMap.GeoCacheMap;

import java.util.LinkedList;

/**
 * Author: Yuri Denison
 * Date: 04.11.2010 21:06:02
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
     * @param id - id of necessary GeoCache
     * @return GeoCache, from favorites if contains or if not, from server.
     */
    public GeoCache getGeoCacheByID(int id) {
        if (favoriteGeoCacheStorage.getGeoCacheByID(id) != null) {
            return favoriteGeoCacheStorage.getGeoCacheByID(id);
        }
        return apiManager.getGeoCacheByID(id);
    }
    
    /**
     * @param maxLatitude - coordinate of the visible area
     * @param minLatitude - coordinate of the visible area
     * @param maxLongitude - coordinate of the visible area
     * @param minLongitude - coordinate of the visible area
     * @param filterList - list of filters (if null - no filter)
     * @return
     */
    public LinkedList<GeoCache> getGeoCacheList(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude, LinkedList<Filter> filterList) {
        if (filterList == null) {
            return apiManager.getGeoCashList(maxLatitude, minLatitude, maxLongitude, minLongitude);
        } else {
            LinkedList<GeoCache> list = apiManager.getGeoCashList(maxLatitude, minLatitude, maxLongitude, minLongitude);
            for (Filter filter : filterList) {
                list = filter.filter(list);
            }
            return list;
        }
    }

    /**
     * Get favorite GeoCaches filtered with chosen filters
     *
     * @param filterList - list of filters (if null - no filter)
     * @return LinkedList<GeoCache>
     */
    public LinkedList<GeoCache> getFavoriteGeoCaches(LinkedList<Filter> filterList) {
        if (filterList == null) {
            return favoriteGeoCacheStorage.getGeoCacheList();
        } else {
            LinkedList<GeoCache> list = favoriteGeoCacheStorage.getGeoCacheList();
            for (Filter filter : filterList) {
                list = filter.filter(list);
            }
            return list;
        }
    }

    /**
     * @return List of geoCaches filters from settings
     */
    public LinkedList<Filter> getFilterList() {
        LinkedList<Filter> list = new LinkedList<Filter>();
        list.addAll(settingsStorage.getFilters());
        return list;
    }

    /**
     * @param geoCache we want to draw on the map
     * @param map      -
     * @return Drawable for this geoCache depends on it's parameters
     */
    public Drawable getMarker(GeoCache geoCache, GeoCacheMap map) {
        //TODO: add different icons for different types of geoCache
        if (geoCache.getParam().get("status").equals("valid")) {
            if (geoCache.getParam().get("type").equals("traditional")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("virtual")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("step by step")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("extreme")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("event")) {
                return getMarker(R.drawable.orangecache, map);
            }
        }
        if (geoCache.getParam().get("status").equals("not valid")) {
            if (geoCache.getParam().get("type").equals("traditional")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("virtual")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("step by step")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("extreme")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("event")) {
                return getMarker(R.drawable.orangecache, map);
            }
        }
        if (geoCache.getParam().get("status").equals("not confirmed that it is not valid")) {
            if (geoCache.getParam().get("type").equals("traditional")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("virtual")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("step by step")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("extreme")) {
                return getMarker(R.drawable.orangecache, map);
            }
            if (geoCache.getParam().get("type").equals("event")) {
                return getMarker(R.drawable.orangecache, map);
            }
        }
        return null;
    }

    private Drawable getMarker(int resource, GeoCacheMap map) {
        Drawable cacheMarker = map.getResources().getDrawable(resource);
        cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(),
                cacheMarker.getMinimumWidth(), 0);
        return cacheMarker;
    }
}

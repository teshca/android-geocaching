package su.geocaching.android.controller;

import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.model.dataStorage.GeoCacheStorage;
import su.geocaching.android.model.dataType.GeoCache;

import java.util.LinkedList;

/**
 * Author: Yuri Denison
 * Date: 04.11.2010 21:06:02
 */
public class Controller {
    private static Controller instance;

    private ApiManager apiManager;
    private GeoCacheStorage favoriteGeoCacheStorage;


    private Controller() {
        apiManager = ApiManager.getInstance();
        favoriteGeoCacheStorage = GeoCacheStorage.getInstance();
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
     * Get GeoCache with necessary id
     *
     * @param id
     * @return GeoCache, from favorites if contains or if not, from server.
     */
    public GeoCache getGeoCacheByID(int id) {
        if (favoriteGeoCacheStorage.getGeoCacheByID(id) != null) {
            return favoriteGeoCacheStorage.getGeoCacheByID(id);
        }
        return apiManager.getGeoCacheByID(id);
    }

    /**
     * Get a list of GeoCaches in radius of the GeoPoint (latitudeE6, longitudeE6)
     *
     * @param latitudeE6
     * @param longitudeE6
     * @param radius
     * @return LinkedList<GeoCache>
     */
    public LinkedList<GeoCache> getGeoCacheList(int latitudeE6, int longitudeE6, float radius) {
        return (LinkedList<GeoCache>) apiManager.getGeoCashList(latitudeE6, longitudeE6, radius);
    }

    /**
     * Get favorite GeoCaches
     *
     * @return LinkedList<GeoCache>
     */
    public LinkedList<GeoCache> getFavoriteGeoCaches() {
        return favoriteGeoCacheStorage.getGeoCacheList();
    }
}

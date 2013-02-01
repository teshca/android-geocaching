package su.geocaching.android.controller.apimanager;

import su.geocaching.android.model.GeoCache;

import java.net.URL;
import java.util.List;

/**
 * @author Nikita Bumakov
 */
public interface IApiManager {

    public static final String UTF8_ENCODING = "UTF-8";
    public static final String CP1251_ENCODING = "windows-1251";

    /**
     * Get list of GeoCache in search area. This method synchronous.
     */
    public List<GeoCache> getGeoCacheList(GeoRect rect);

    /**
     * Get info about geocache.This method synchronous.
     */
    public String getInfo(int cacheId);
    
    /**
     * Get notebook of geocache.This method synchronous.
     */
    public String getNotebook(int cacheId); 
    
    /**
     * Get list of photos of geocache.This method synchronous.
     */
    public List<URL> getPhotoList(int cacheId);  
    
    /**
     * Download photo image from the given URL and save it to external storage.
     */
    public Boolean downloadPhoto(int cacheId, URL photoUrl);
}
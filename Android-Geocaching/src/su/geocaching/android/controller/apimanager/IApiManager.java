package su.geocaching.android.controller.apimanager;

import java.net.URL;
import java.util.List;

import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.info.InfoActivity;

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
     * Get info/notebook about geocache. This method asynchronous.
     */
    public void getInfo(DownloadInfoState state, InfoActivity infoActivity, int cacheId);

    /**
     * Get photo about geocache. This method asynchronous.
     */
    public void getPhotos(InfoActivity infoActivity, int cacheId);
}
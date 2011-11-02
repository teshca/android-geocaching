package su.geocaching.android.controller.apimanager;

import java.util.List;
import java.util.Locale;

import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.info.InfoActivity;
import android.content.Context;

import com.google.android.maps.GeoPoint;

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
     * Get info/notebook about geocache. This method asynchronous.
     */
    public void getInfo(Context context, DownloadInfoState state, InfoActivity infoActivity, int cacheId);

    /**
     * Get photo about geocache. This method asynchronous.
     */
    public void getPhotos(Context context, InfoActivity infoActivity, int cacheId);
}
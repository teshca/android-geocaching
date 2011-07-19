package su.geocaching.android.controller.apimanager;

import java.util.List;

import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.info.InfoActivity;
import android.content.Context;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.ui.info.InfoActivity;

/**
 * @author Nikita Bumakov
 */
public interface IApiManager {

    /**
     * Get list of GeoCache in search area
     * 
     * @param upperLeftCorner
     *            //TODO describe it
     * @param lowerRightCorner
     *            //TODO describe it
     * @return List of geoCash in visible area
     */
    public List<GeoCache> getGeoCacheList(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner);

    public void downloadInfo(Context context, DownloadInfoState state, InfoActivity infoActivity, int cacheId);

    public void downloadPhotos(Context context, InfoActivity infoActivity, int cacheId);
}
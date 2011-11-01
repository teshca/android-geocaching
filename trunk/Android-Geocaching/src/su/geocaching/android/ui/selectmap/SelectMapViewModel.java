package su.geocaching.android.ui.selectmap;

import android.os.AsyncTask;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.DownloadGeoCachesTask;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.selectmap.geocachegroup.GroupGeoCacheTask;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps the state of current select map
 */
public class SelectMapViewModel {
    private static final String TAG = SelectMapViewModel.class.getCanonicalName();
    private static final int MIN_GROUP_CACHE_NUMBER = 8;

    private int downloadTasksCount = 0;
    private SelectMapActivity activity;

    private GroupGeoCacheTask groupTask = null;
    private List<GeoCacheOverlayItem> currentGeoCacheOverlayItems = new LinkedList<GeoCacheOverlayItem>();

    private Projection projection;
    private int mapWidth, mapHeight;

    public void beginUpdateGeocacheOverlay(Projection projection, int mapWidth, int mapHeight) {
        cancelGroupTask();
        final GeoPoint upperLeftCorner = projection.fromPixels(0, 0);
        final GeoPoint lowerRightCorner = projection.fromPixels(mapWidth, mapHeight);
        GeoPoint[] d = {upperLeftCorner, lowerRightCorner};
        new DownloadGeoCachesTask(this).execute(d);
        increaseDownloadTaskCount();

        this.projection = projection;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public synchronized void geocacheListDownloaded(List<GeoCache> geoCacheList) {
        decreaseDownloadTaskCount();
        if (geoCacheList == null || geoCacheList.size() == 0) {
            return;
        }
        if (Controller.getInstance().getPreferencesManager().isCacheGroupingEnabled() && geoCacheList.size() > MIN_GROUP_CACHE_NUMBER) {
            beginGroupGeoCacheList(geoCacheList);
        } else {
            currentGeoCacheOverlayItems.clear();
            for (GeoCache geoCache : geoCacheList) {
                currentGeoCacheOverlayItems.add(new GeoCacheOverlayItem(geoCache, "", ""));
            }
            onUpdateGeocacheOverlay();
        }
    }

    private synchronized void beginGroupGeoCacheList(List<GeoCache> geoCacheList) {
        cancelGroupTask();
        groupTask = new GroupGeoCacheTask(this, geoCacheList);
        groupTask.execute();
        onShowGroupingInfo();
    }

    public synchronized void geocacheListGrouped(List<GeoCacheOverlayItem> geoCacheList) {
        onHideGroupingInfo();
        currentGeoCacheOverlayItems.clear();
        for (GeoCacheOverlayItem geoCacheOverlayItem : geoCacheList) {
            currentGeoCacheOverlayItems.add(geoCacheOverlayItem);
        }
        onUpdateGeocacheOverlay();
    }

    private synchronized void cancelGroupTask() {
        if (groupTask != null && !groupTask.isCancelled()) {
            groupTask.cancel(true);
        }
    }

    private synchronized void onUpdateGeocacheOverlay() {
        if (activity != null) {
            activity.updateGeoCacheOverlay(currentGeoCacheOverlayItems);
        }
    }

    public Projection getProjection()
    {
        return projection;
    }

    public int getMapHeight()
    {
        return mapHeight;
    }

    public int getMapWidth()
    {
        return mapWidth;
    }

    private synchronized void increaseDownloadTaskCount() {
        if (downloadTasksCount == 0) {
            onShowDownloadingInfo();
        }
        downloadTasksCount++;
    }

    private synchronized void decreaseDownloadTaskCount() {
        downloadTasksCount--;
        if (downloadTasksCount == 0) {
            onHideDownloadingInfo();
        }
    }

    private synchronized void onHideDownloadingInfo() {
        if (activity != null) {
            activity.hideDownloadingInfo();
        }
    }

    private synchronized void onShowDownloadingInfo() {
        if (activity != null) {
            activity.showDownloadingInfo();
        }
    }

    private synchronized void onShowGroupingInfo() {
        if (activity != null) {
            activity.showGroupingInfo();
        }
    }

    private synchronized void onHideGroupingInfo() {
        if (activity != null) {
            activity.hideGroupingInfo();
        }
    }

    public synchronized void registerActivity(SelectMapActivity activity) {
        if (this.activity != null) {
            LogManager.e(TAG, "Attempt to register activity while activity is not null");
        }
        this.activity = activity;
        // update activity state
        if (groupTask != null && !groupTask.isCancelled() && (groupTask.getStatus() != AsyncTask.Status.FINISHED)) {
            onShowGroupingInfo();
        }
        if (downloadTasksCount != 0) {
            onShowDownloadingInfo();
        }
    }

    public synchronized void unregisterActivity(SelectMapActivity activity) {
        if (this.activity == null) {
            LogManager.e(TAG, "Attempt to unregister activity while activity is null");
        }
        this.activity = null;
        cancelGroupTask();
    }
}
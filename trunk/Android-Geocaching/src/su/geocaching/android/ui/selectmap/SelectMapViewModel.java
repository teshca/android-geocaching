package su.geocaching.android.ui.selectmap;

import android.os.AsyncTask;
import com.google.android.gms.maps.Projection;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.DownloadGeoCachesTask;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.selectmap.geocachegroup.GroupGeoCacheTask;
import su.geocaching.android.model.GeoCache;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps the state of current select map
 */
public class SelectMapViewModel {
    private static final String TAG = SelectMapViewModel.class.getCanonicalName();
    private static final int MIN_GROUP_CACHE_NUMBER = 8;
    private static final int MAX_OVERLAY_ITEMS_NUMBER = 100;

    private SelectMapActivity activity;

    private GroupGeoCacheTask groupTask = null;
    private DownloadGeoCachesTask downloadTask = null;
    // TODO: also keep current viewport, don't run any update if viewport is the same
    private List<GeoCache> currentGeoCacheMarkers = new LinkedList<GeoCache>();

    private Projection projection;
    private int mapWidth, mapHeight;

    public synchronized void beginUpdateGeocacheOverlay(GeoRect viewPort, Projection projection, int mapWidth, int mapHeight) {
        cancelGroupTask();

        LogManager.d(TAG, "Update rectangle %s", viewPort);
        cancelDownloadTask();
        downloadTask = new DownloadGeoCachesTask(this);
        downloadTask.execute(viewPort);
        onShowDownloadingInfo();

        this.projection = projection;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public synchronized void geocacheListDownloaded(List<GeoCache> geoCacheList) {
        onHideDownloadingInfo();
        if (geoCacheList == null || geoCacheList.size() == 0) {
            return;
        }
        if (Controller.getInstance().getPreferencesManager().isCacheGroupingEnabled() && geoCacheList.size() > MIN_GROUP_CACHE_NUMBER) {
            beginGroupGeoCacheList(geoCacheList);
        } else {
            currentGeoCacheMarkers.clear();
            if (geoCacheList.size() < MAX_OVERLAY_ITEMS_NUMBER) {
                for (GeoCache geoCache : geoCacheList) {
                    currentGeoCacheMarkers.add(geoCache);
                }
                onUpdateGeocacheMarkers();
            } else {
                onTooManyOverlayItems();
            }
        }
    }

    private synchronized void beginGroupGeoCacheList(List<GeoCache> geoCacheList) {
        cancelGroupTask();
        groupTask = new GroupGeoCacheTask(this, geoCacheList);
        groupTask.execute();
        onShowGroupingInfo();
    }

    public synchronized void geocacheListGrouped(List<GeoCache> geoCacheList) {
        onHideGroupingInfo();
        currentGeoCacheMarkers.clear();
        for (GeoCache geoCache : geoCacheList) {
            currentGeoCacheMarkers.add(geoCache);
        }
        onUpdateGeocacheMarkers();
    }

    public synchronized void groupTaskCancelled() {
        onHideGroupingInfo();
    }

    private synchronized void cancelGroupTask() {
        if (groupTask != null && !groupTask.isCancelled()) {
            groupTask.cancel(true);
        }
    }

    private synchronized void cancelDownloadTask() {
        if (downloadTask != null && !downloadTask.isCancelled()) {
            // don't interrupt if already running
            downloadTask.cancel(false);
        }
    }

    private synchronized void onUpdateGeocacheMarkers() {
        if (activity != null) {
            activity.updateGeoCacheMarkers(currentGeoCacheMarkers);
        }
    }

    private synchronized void onTooManyOverlayItems() {
        if (activity != null) {
            activity.tooManyOverlayItems();
        }
    }

    public Projection getProjection() {
        return projection;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
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
        // display [grouped] caches
        onUpdateGeocacheMarkers();
        // update activity state
        if (groupTask != null && !groupTask.isCancelled() && (groupTask.getStatus() != AsyncTask.Status.FINISHED)) {
            onShowGroupingInfo();
        }
        if (downloadTask != null && !downloadTask.isCancelled() && (downloadTask.getStatus() != AsyncTask.Status.FINISHED)) {
            onShowDownloadingInfo();
        }
    }

    public synchronized void unregisterActivity(SelectMapActivity activity) {
        if (this.activity == null) {
            LogManager.e(TAG, "Attempt to unregister activity while activity is null");
        }
        this.activity = null;
        cancelDownloadTask();
        cancelGroupTask();
    }
}
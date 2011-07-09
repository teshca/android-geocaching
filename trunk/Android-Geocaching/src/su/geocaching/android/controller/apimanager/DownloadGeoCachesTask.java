package su.geocaching.android.controller.apimanager;

import android.os.AsyncTask;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.selectmap.SelectMapActivity;

import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

/**
 * Class downloads List of GeoCaches and adds them to SelectMapActivity
 * 
 * @author Nikita Bumakov
 */
public class DownloadGeoCachesTask extends AsyncTask<GeoPoint, Integer, List<GeoCache>> {
    private final SelectMapActivity map;
    private final IApiManager apiManager;
    private final Controller controller;
    private static final int MIN_GROUP_CACHE_NUMBER = 8;

    public DownloadGeoCachesTask(IApiManager apiManager, SelectMapActivity map) {
        this.apiManager = apiManager;
        this.map = map;
        controller = Controller.getInstance();
    }

    @Override
    protected List<GeoCache> doInBackground(GeoPoint... params) {
        List<GeoCache> gcList = apiManager.getGeoCacheList(params[0], params[1]);
        filterCacheList(gcList);
        return gcList;
    }

    private synchronized void filterCacheList(List<GeoCache> list) {
        EnumSet<GeoCacheType> typeSet = controller.getPreferencesManager().getTypeFilter();
        EnumSet<GeoCacheStatus> statusSet = controller.getPreferencesManager().getStatusFilter();

        ListIterator<GeoCache> iterator = list.listIterator();
        while (iterator.hasNext()) {
            GeoCache cache = iterator.next();
            if (!(typeSet.contains(cache.getType()) && statusSet.contains(cache.getStatus()))) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void onPostExecute(List<GeoCache> gcList) {
        if (!isCancelled()) {
            if (Controller.getInstance().getPreferencesManager().getAddingCacheWayString() && gcList.size() > MIN_GROUP_CACHE_NUMBER) {
                map.groupUseAddGeoCacheList(gcList);
            } else {
                map.simpleAddGeoCacheList(gcList);
            }
        }
    }
}

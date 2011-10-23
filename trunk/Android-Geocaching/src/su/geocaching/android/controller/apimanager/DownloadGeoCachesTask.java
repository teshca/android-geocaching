package su.geocaching.android.controller.apimanager;

import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.selectmap.SelectMapActivity;
import android.os.AsyncTask;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.ui.selectmap.SelectMapViewModel;

/**
 * Class downloads List of GeoCaches and adds them to SelectMapActivity
 * 
 * @author Nikita Bumakov
 */
public class DownloadGeoCachesTask extends AsyncTask<GeoPoint, Integer, List<GeoCache>> {
    private final SelectMapViewModel selectMapViewModel;

    public DownloadGeoCachesTask(SelectMapViewModel selectMapViewModel) {
        this.selectMapViewModel = selectMapViewModel;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected List<GeoCache> doInBackground(GeoPoint... params) {
        List<GeoCache> gcList = Controller.getInstance().getApiManager().getGeoCacheList(params[0], params[1]);
        filterCacheList(gcList);
        return gcList;
    }

    private synchronized void filterCacheList(List<GeoCache> list) {
        EnumSet<GeoCacheType> typeSet = Controller.getInstance().getPreferencesManager().getTypeFilter();
        EnumSet<GeoCacheStatus> statusSet = Controller.getInstance().getPreferencesManager().getStatusFilter();

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
        selectMapViewModel.geocacheListDownloaded(gcList);
    }
}

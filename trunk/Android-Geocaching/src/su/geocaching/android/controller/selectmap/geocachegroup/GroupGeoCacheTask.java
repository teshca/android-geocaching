package su.geocaching.android.controller.selectmap.geocachegroup;

import android.os.AsyncTask;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.selectmap.SelectMapViewModel;

import java.util.List;

/**
 * @author: Yuri Denison
 * @since: 19.02.11
 */
public class GroupGeoCacheTask extends AsyncTask<Void, Integer, List<GeoCacheOverlayItem>> {
    private final String TAG = GroupGeoCacheTask.class.getCanonicalName();
    private final SelectMapViewModel selectMapViewModel;
    private final GeoCacheListAnalyzer analyzer;
    private final List<GeoCache> geoCacheList;

    public GroupGeoCacheTask(SelectMapViewModel selectMapViewModel, List<GeoCache> geoCacheList) {
        this.selectMapViewModel = selectMapViewModel;
        this.geoCacheList = geoCacheList;
        analyzer = new GeoCacheListAnalyzer(selectMapViewModel.getMapView());
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected List<GeoCacheOverlayItem> doInBackground(Void... voids) {
        LogManager.d(TAG, "start doInBackground, par.len = " + geoCacheList.size());
        return analyzer.getGroupedList(geoCacheList, this);
    }

    @Override
    protected void onPostExecute(List<GeoCacheOverlayItem> items) {
        LogManager.d(TAG, "start add Overlay Items, items = " + items.size());
        selectMapViewModel.geocacheListGrouped(items);
    }

    @Override
    protected void onCancelled() {
        LogManager.d(TAG, "Group task cancelled");
    }
}

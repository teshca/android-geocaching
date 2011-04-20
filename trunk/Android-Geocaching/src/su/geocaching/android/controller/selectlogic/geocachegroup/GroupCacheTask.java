package su.geocaching.android.controller.selectlogic.geocachegroup;

import android.os.AsyncTask;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.selectmap.SelectMap;

import java.util.List;

/**
 * @author: Yuri Denison
 * @since: 19.02.11
 */
public class GroupCacheTask extends AsyncTask<Void, Integer, List<GeoCacheOverlayItem>> {
    private final SelectMap map;
    private final GeoCacheListAnalyzer analyzer;
    private final String TAG = "GroupCacheTask";
    private final List<GeoCache> geoCacheList;

    public GroupCacheTask(SelectMap map, List<GeoCache> geoCacheList) {
        this.map = map;
        this.geoCacheList = geoCacheList;
        analyzer = new GeoCacheListAnalyzer(map.getMapView());
    }

    @Override
    protected List<GeoCacheOverlayItem> doInBackground(Void... voids) {
        LogManager.d(TAG, "start doInBackground, par.len = " + geoCacheList.size());
        return analyzer.getList(geoCacheList);
    }

    @Override
    protected void onPostExecute(List<GeoCacheOverlayItem> items) {
        if (!isCancelled()) {
            LogManager.d(TAG, "start add OItems, items = " + items.size());
            map.addOverlayItemList(items);
            LogManager.d(TAG, "finish add OItems");
        }
    }
}

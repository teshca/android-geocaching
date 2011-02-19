package su.geocaching.android.ui.selectgeocache.geocachegroup;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.maps.MapView;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Yuri Denison
 * @since: 19.02.11
 */
public class GroupCacheTask extends AsyncTask<List<GeoCache>, Integer, List<GeoCacheOverlayItem>>{
    private SelectGeoCacheMap map;
    private GeoCacheListAnalyzer analyzer;
    private static final String TAG = "GroupCacheTask";

    public GroupCacheTask(SelectGeoCacheMap map) {
        this.map = map;
        analyzer = new GeoCacheListAnalyzer(map.getMapView());
    }

    @Override
    protected List<GeoCacheOverlayItem> doInBackground(List<GeoCache>... params) {
        Log.d(TAG, "start doInBackground, par.len = " + params[0].size());
        return analyzer.getList(params[0]);
    }

    @Override
    protected void onPostExecute(List<GeoCacheOverlayItem> items) {
        Log.d(TAG, "start add OItems, items = " + items.size());
        map.addOverlayItemList(items);
        Log.d(TAG, "finish add OItems");
    }
}

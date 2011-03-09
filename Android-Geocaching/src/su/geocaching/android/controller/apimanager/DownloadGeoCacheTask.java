package su.geocaching.android.controller.apimanager;

import android.os.AsyncTask;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

import java.util.List;

/**
 * Class downloads List of GeoCaches and adds them to SelectGeoCacheMap
 *
 * @author Nikita Bumakov
 */
public class DownloadGeoCacheTask extends AsyncTask<GeoPoint, Integer, List<GeoCache>> {
    private SelectGeoCacheMap map;
    private IApiManager apiManager;

    public DownloadGeoCacheTask(IApiManager apiManager, SelectGeoCacheMap map) {
        this.apiManager = apiManager;
        this.map = map;
    }

    @Override
    protected List<GeoCache> doInBackground(GeoPoint... params) {
        List<GeoCache> gcList = apiManager.getGeoCacheList(params[0], params[1]);
        map.filterCacheList(gcList);
        return gcList;
    }

    @Override
    protected void onPostExecute(List<GeoCache> gcList) {
        if (map.getWayCacheAdding()) {
            map.testAddGeoCacheList(gcList);
        } else {
            map.addGeoCacheList(gcList);
        }
    }
}

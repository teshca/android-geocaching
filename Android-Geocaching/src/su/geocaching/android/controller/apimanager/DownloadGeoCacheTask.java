package su.geocaching.android.controller.apimanager;

import android.os.AsyncTask;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

import java.util.LinkedList;
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
        return gcList;
    }

    @Override
    protected void onPostExecute(List<GeoCache> gcList) {
        Controller controller = Controller.getInstance();
        LinkedList<IFilter> filterList = controller.getFilterList();

        if (filterList != null) {
            for (IFilter filter : filterList) {
                gcList = filter.filter(gcList);
            }
        }
        if (map.getWayCacheAdding()) {
            map.testAddGeoCacheList(gcList);
        } else {
            map.addGeoCacheList(gcList);
        }
    }
}

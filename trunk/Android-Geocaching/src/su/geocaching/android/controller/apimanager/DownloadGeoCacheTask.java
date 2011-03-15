package su.geocaching.android.controller.apimanager;

import android.os.AsyncTask;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Class downloads List of GeoCaches and adds them to SelectGeoCacheMap
 *
 * @author Nikita Bumakov
 */
public class DownloadGeoCacheTask extends AsyncTask<GeoPoint, Integer, List<GeoCache>> {
    private SelectGeoCacheMap map;
    private IApiManager apiManager;
    private Controller controller;


    public DownloadGeoCacheTask(IApiManager apiManager, SelectGeoCacheMap map) {
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
        if (Controller.getInstance().getPreferencesManager().getAddingCacheWayString()) {
            map.testAddGeoCacheList(gcList);
        } else {
            map.addGeoCacheList(gcList);
        }
    }
}

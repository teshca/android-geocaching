package su.geocaching.android.controller.apimanager;

import android.content.Context;
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

    private List<GeoCacheType> typeFilterList;
    private List<GeoCacheStatus> statusFilterList;
    private Controller controller;

    private void initFilterLists() {
        typeFilterList = new LinkedList<GeoCacheType>();
        statusFilterList = new LinkedList<GeoCacheStatus>();
        Context mapContext = map.getMapView().getContext();

        for (GeoCacheType type : EnumSet.allOf(GeoCacheType.class)) {
            if (controller.getTypeFilter(mapContext, type)) {
                typeFilterList.add(type);
            }
        }

        for (GeoCacheStatus status : EnumSet.allOf(GeoCacheStatus.class)) {
            if (controller.getStatusFilter(mapContext, status)) {
                statusFilterList.add(status);
            }
        }
    }

    public DownloadGeoCacheTask(IApiManager apiManager, SelectGeoCacheMap map) {
        this.apiManager = apiManager;
        this.map = map;
        controller = Controller.getInstance();
    }

    @Override
    protected List<GeoCache> doInBackground(GeoPoint... params) {
        List<GeoCache> gcList = apiManager.getGeoCacheList(params[0], params[1]);
        initFilterLists();
        filterCacheList(gcList);
        return gcList;
    }

    private synchronized void filterCacheList(List<GeoCache> list) {
        ListIterator<GeoCache> iterator = list.listIterator();
        while (iterator.hasNext()) {
            GeoCache cache = iterator.next();
            if (!(typeFilterList.contains(cache.getType()) && statusFilterList.contains(cache.getStatus()))) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void onPostExecute(List<GeoCache> gcList) {
        if (Controller.getInstance().getWayCacheAdding(map.getMapView().getContext())) {
            map.testAddGeoCacheList(gcList);
        } else {
            map.addGeoCacheList(gcList);
        }
    }
}

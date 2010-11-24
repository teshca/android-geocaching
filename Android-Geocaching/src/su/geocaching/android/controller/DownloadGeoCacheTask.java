package su.geocaching.android.controller;

import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import android.os.AsyncTask;

/**
 * @author Nikita Bumakov
 * <p> Class downloads List of GeoCahes and adds them to SelectGeoCacheMap </p>
 */
class DownloadGeoCacheTask extends AsyncTask<Double, Integer, List<GeoCache>> {
    private SelectGeoCacheMap map;
    private IApiManager apiManager;

    public DownloadGeoCacheTask(IApiManager apiManager, SelectGeoCacheMap map) {
        this.apiManager = apiManager;
	this.map = map;        
    }

    @Override
    protected List<GeoCache> doInBackground(Double... params) {	
	List<GeoCache> gcList = apiManager.getGeoCacheList(params[0], params[1], params[2], params[3]);
	return gcList;
    }

    @Override
    protected void onPostExecute(List<GeoCache> gcList) {
	LinkedList<IFilter> filterList = Controller.getInstance().getFilterList();

	if (filterList != null) {
	    for (IFilter filter : filterList) {
		gcList = filter.filter(gcList);
	    }
	}
	map.addGeoCacheList(gcList);
    }
}

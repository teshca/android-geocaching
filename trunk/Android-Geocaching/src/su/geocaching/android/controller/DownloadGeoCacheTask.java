package su.geocaching.android.controller;

import java.util.LinkedList;

import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import android.os.AsyncTask;

/**
 * @author Nikita Bumakov
 * <p> Class downloads List of GeoCahes and adds them to SelectGeoCacheMap </p>
 */
class DownloadGeoCacheTask extends AsyncTask<Double, Integer, LinkedList<GeoCache>> {

    @Override
    protected LinkedList<GeoCache> doInBackground(Double... params) {
	ApiManager apiManager = ApiManager.getInstance();
	LinkedList<GeoCache> gkList = apiManager.getGeoCashList(params[0], params[1], params[2], params[3]);
	return gkList;
    }

    @Override
    protected void onPostExecute(LinkedList<GeoCache> gcList) {
	LinkedList<IFilter> filterList = Controller.getInstance().getFilterList();

	if (filterList != null) {
	    for (IFilter filter : filterList) {
		gcList = filter.filter(gcList);
	    }
	}
	SelectGeoCacheMap.getInstance().addGeoCacheList(gcList);
    }
}

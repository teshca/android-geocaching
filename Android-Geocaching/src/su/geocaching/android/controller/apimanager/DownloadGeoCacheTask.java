package su.geocaching.android.controller.apimanager;

import java.util.LinkedList;
import java.util.List;

import com.google.android.maps.GeoPoint;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import android.os.AsyncTask;

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
		LinkedList<IFilter> filterList = Controller.getInstance().getFilterList();

		if (filterList != null) {
			for (IFilter filter : filterList) {
				gcList = filter.filter(gcList);
			}
		}
		map.testAddGeoCacheList(gcList);
	}
}

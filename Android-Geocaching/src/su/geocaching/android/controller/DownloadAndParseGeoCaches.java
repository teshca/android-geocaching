package su.geocaching.android.controller;

import java.util.LinkedList;

import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.model.datatype.GeoCache;

import android.os.AsyncTask;

class DownloadAndParseGeoCaches extends AsyncTask<Double, Integer, LinkedList<GeoCache>> {

    @Override
    protected LinkedList<GeoCache> doInBackground(Double... params) {
	ApiManager apiManager = ApiManager.getInstance();
	LinkedList<GeoCache> gkList = apiManager.getGeoCashList(params[0], params[1], params[2], params[3]);
	return gkList;  
    }
}

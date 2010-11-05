package su.geocaching.android.controller.apimanager;

import su.geocaching.android.model.dataType.GeoCache;

import java.util.LinkedList;

public interface IApiManager {

    public abstract LinkedList<GeoCache> getGeoCashList(int latitudeE6, int longitudeE6, float radius);

}
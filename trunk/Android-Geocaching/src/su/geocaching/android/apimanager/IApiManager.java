package su.geocaching.android.apimanager;

import java.util.Collection;

import su.geocaching.android.model.GeoCache;

public interface IApiManager {

    public abstract Collection<GeoCache> getGeoCashList(int latitudeE6, int longitudeE6, float radius);

}
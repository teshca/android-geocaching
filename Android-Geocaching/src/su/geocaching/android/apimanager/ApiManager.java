package su.geocaching.android.apimanager;

import java.util.ArrayList;
import java.util.Collection;

import su.geocaching.android.model.GeoCache;

public class ApiManager implements IApiManager {
   
    @Override
    public Collection<GeoCache> getGeoCashList(int latitudeE6, int longitudeE6, float radius){
	ArrayList<GeoCache> geoCaches = new ArrayList<GeoCache>(10);
	
	//TODO
	//---------------
	geoCaches.add(new GeoCache(59879429, 29830010, 0));
	geoCaches.add(new GeoCache(59881754, 29830850, 1));
	geoCaches.add(new GeoCache(59882637, 29823261, 2));
	geoCaches.add(new GeoCache(59878740, 29833766, 3));
	geoCaches.add(new GeoCache(59875185, 29825790, 4));
	geoCaches.add(new GeoCache(59875078, 29827163, 5));
	geoCaches.add(new GeoCache(59874324, 29830080, 6));
	geoCaches.add(new GeoCache(59886449, 29832858, 7));
	geoCaches.add(new GeoCache(59879709, 29862064, 8));
	geoCaches.add(new GeoCache(59848672, 29879713, 9));
	//---------------
	return geoCaches;
    }
}

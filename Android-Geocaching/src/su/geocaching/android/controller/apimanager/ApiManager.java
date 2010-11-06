package su.geocaching.android.controller.apimanager;

import su.geocaching.android.model.dataType.GeoCache;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import android.os.Handler;
import android.util.Log;

public class ApiManager implements IApiManager {
    private static ApiManager instance;
    private LinkedList<GeoCache> geoCaches;
    private String URL = "http://www.geocaching.su/pages/1031.ajax.php";

    private ApiManager() {
    }

    public static ApiManager getInstance() {
	if (instance == null) {
	    synchronized (ApiManager.class) {
		if (instance == null) {
		    instance = new ApiManager();
		}
	    }
	}
	return instance;
    }

    @Override
    public LinkedList<GeoCache> getGeoCashList(int latitudeE6, int longitudeE6, float radius) {
	geoCaches = new LinkedList<GeoCache>();

	// TODO
	// ---------------
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
	// ---------------
	GeoCacheSaxHandler handler = null;
	try {
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    // TODO
	    URL url = new URL(URL + "?lngmax=41.15478515625&lngmin=22.3681640625&latmax=62.20651189841766&latmin=57.320589769167135&id=1738361&geocaching=d6970489a98b83cb7382f7db94d574df");
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
		return null;
	    }
	    InputSource courseXml = new InputSource(new InputStreamReader(connection.getInputStream(), "windows-1251"));
	    handler = new GeoCacheSaxHandler();
	    parser.parse(courseXml, handler);
	} catch (Exception e) {
	    Log.e("", e.getMessage(), e);
	}

	//return handler.getGeoCaches();
	return geoCaches;
    }

    public GeoCache getGeoCacheByID(int id) {
	for (GeoCache geoCache : geoCaches) {
	    if (geoCache.getId() == id) {
		return geoCache;
	    }
	}
	return null;
    }
}

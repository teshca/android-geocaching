package su.geocaching.android.controller.apimanager;

import android.util.Log;
import android.webkit.URLUtil;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datatype.GeoCache;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

/**
 * @author Nikita Bumakov
 * 
 *         Class for getting data from Geocaching.su. This class implements
 *         IApiManager
 */
public class ApiManager implements IApiManager {

    private static final String TAG = ApiManager.class.getCanonicalName();
    private static ApiManager instance;

    private LinkedList<GeoCache> geoCaches;

    private ApiManager() {
    }

    public static ApiManager getInstance() {
	if (instance == null) {
	    synchronized (Controller.class) {
		if (instance == null) {
		    instance = new ApiManager();
		}
	    }
	}
	return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.controller.apimanager.IApiManager#getGeoCashList
     * (double, double, double, double)
     */

    @Override
    public LinkedList<GeoCache> getGeoCashList(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
	Log.d(TAG, "getGeoCashList");

	geoCaches = new LinkedList<GeoCache>();

	GeoCacheSaxHandler handler = null;
	HttpURLConnection connection = null;
	try {
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    URL url = generateUrl(maxLatitude, minLatitude, maxLongitude, minLongitude);
	    connection = (HttpURLConnection) url.openConnection();

	    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
		// TODO make real error message
		Log.e(TAG, "Cann't connect ti internet");
	    }
	    InputSource courseXml = new InputSource(new InputStreamReader(connection.getInputStream(), ENCODING));
	    handler = new GeoCacheSaxHandler();
	    parser.parse(courseXml, handler);
	    geoCaches = handler.getGeoCaches();
	} catch (MalformedURLException e) {
	    Log.e(TAG, e.getMessage(), e);
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage(), e);
	} catch (SAXException e) {
	    Log.e(TAG, e.getMessage(), e);
	} catch (ParserConfigurationException e) {
	    Log.e(TAG, e.getMessage(), e);
	} finally {
	    if (connection != null) {
		connection.disconnect();
	    }
	}

	return geoCaches;
    }

    private URL generateUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
	String GEOCACHING_PARAM = "abc"; // I don't know what it is....
	int id = (int) (Math.random() * 1E6);
	String request = String.format("%s%s%f%s%f%s%f%s%f%s%d%s%s", URL, "?lngmax=", maxLongitude, "&lngmin=", minLongitude, "&latmax=", maxLatitude, "&latmin=", minLatitude, "&id=", id,
		"&geocaching=", GEOCACHING_PARAM);
	return new URL(request);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.controller.apimanager.IApiManager#getGeoCacheByID
     * (int)
     */

    @Override
    public GeoCache getGeoCacheByID(int id) {
	for (GeoCache geoCache : geoCaches) {
	    if (geoCache.getId() == id) {
		return geoCache;
	    }
	}
	return null;
    }

    private String URL = "http://www.geocaching.su/pages/1031.ajax.php";
    private static final String ENCODING = "windows-1251";
}
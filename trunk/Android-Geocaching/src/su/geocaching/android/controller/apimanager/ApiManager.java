package su.geocaching.android.controller.apimanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import su.geocaching.android.model.datatype.GeoCache;
import android.util.Log;

/**
 * @author Nikita Bumakov
 *         <p>
 *         Class for getting data from Geocaching.su. This class implements
 *         IApiManager
 *         </p>
 */
public class ApiManager implements IApiManager {

    private static final String TAG = ApiManager.class.getCanonicalName();
    private static final String URL = "http://www.geocaching.su/pages/1031.ajax.php";
    private static final String ENCODING = "windows-1251";

    private List<GeoCache> geoCaches;
    private static int id;

    public ApiManager() {
	id = (int) (Math.random() * 1E6);
	geoCaches = new LinkedList<GeoCache>();
	Log.d(TAG, "new ApiManager Created");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.controller.apimanager.IApiManager#getGeoCacheList
     * (double, double, double, double)
     */
    @Override
    public List<GeoCache> getGeoCacheList(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
	Log.d(TAG, "getGeoCacheList");

	GeoCacheSaxHandler handler = null;
	HttpURLConnection connection = null;
	try {
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    URL url = generateUrl(maxLatitude, minLatitude, maxLongitude, minLongitude);
	    connection = (HttpURLConnection) url.openConnection();

	    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
		Log.e(TAG, "Can't connect to geocaching.su. Response: " + connection.getResponseCode());
	    }

	    InputSource geoCacheXml = new InputSource(new InputStreamReader(connection.getInputStream(), ENCODING));
	    handler = new GeoCacheSaxHandler();
	    parser.parse(geoCacheXml, handler);
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

	Log.d(TAG, "Size of obtained listGeoCaches: " + geoCaches.size());
	return geoCaches;
    }

    private URL generateUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
	String request = String.format(URL + "?lngmax=%f&lngmin=%f&latmax=%f&latmin=%f&id=%d", maxLongitude, minLongitude, maxLatitude, minLatitude, id);
	Log.d(TAG, "generated Url: " + request);
	return new URL(request);
    }

}
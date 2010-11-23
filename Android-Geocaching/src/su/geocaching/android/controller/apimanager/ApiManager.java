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
    private static ApiManager instance;

    private LinkedList<GeoCache> geoCaches;
    private static int id;

    private ApiManager() {
	id = (int) (Math.random() * 1E6);
	Log.d(TAG, "new ApiManager Created");
    }

    /**
     * @return an instance of this class
     */
    public synchronized static IApiManager getInstance() {
	if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
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
		Log.e(TAG, "Can't connect to internet");
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

	Log.d(TAG, "Size of obtained listGeoCaches " + geoCaches.size());
	return geoCaches;
    }

    private URL generateUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
        // TODO: understand what it is
	String GEOCACHING_PARAM = "abc"; // I don't know what it is.... but it's
					 // work
	String request = String.format("%s%s%f%s%f%s%f%s%f%s%d%s%s", URL, "?lngmax=", maxLongitude, "&lngmin=", minLongitude, "&latmax=", maxLatitude, "&latmin=", minLatitude, "&id=", id,
		"&geocaching=", GEOCACHING_PARAM);
	Log.d(TAG, "generated Url: "+request);
	return new URL(request);
    }

    private String URL = "http://www.geocaching.su/pages/1031.ajax.php";
    private static final String ENCODING = "windows-1251";
}
package su.geocaching.android.controller.apimanager;

import android.util.Log;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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
 *         <p/>
 *         Class for getting data from Geocaching.su. This class implements
 *         IApiManager
 */
public class ApiManager implements IApiManager {

    private static final String TAG = "Android GeoCaching. ApiManager";

    private static IApiManager instance;
    private LinkedList<GeoCache> geoCaches;

    private ApiManager() {
    }

    public static IApiManager getInstance() {
        if (instance == null) {
            synchronized (ApiManager.class) {
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
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            URL url = generateUrl(maxLatitude, minLatitude, maxLongitude, minLongitude);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            InputSource courseXml = new InputSource(new InputStreamReader(connection.getInputStream(), ENCODING));
            handler = new GeoCacheSaxHandler();
            parser.parse(courseXml, handler);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return handler.getGeoCaches();
    }

    private URL generateUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
        String request = URL + "?lngmax=" + (maxLongitude) + "&lngmin=" + (minLongitude) + "&latmax=" + (maxLatitude) + "&latmin=" + (minLatitude) + "&id=" + ((int) (Math.random() * 1E6))
                + "&geocaching=" + "d6970489a98b83cb7382f7db94d574df";
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

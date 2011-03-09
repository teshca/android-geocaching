package su.geocaching.android.controller.apimanager;

import com.google.android.maps.GeoPoint;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.utils.log.LogHelper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Class for getting data from Geocaching.su. This class implements IApiManager
 *
 * @author Nikita Bumakov
 */
public class ApiManager implements IApiManager {

    private static final String TAG = ApiManager.class.getCanonicalName();
    private static final String URL = "http://www.geocaching.su/pages/1031.ajax.php";
    private static final String ENCODING = "windows-1251";

    private List<GeoCache> geoCaches;
    private Locale rusLocale;
    private int id;

    public ApiManager() {
        id = (int) (Math.random() * 1E6);
        geoCaches = new LinkedList<GeoCache>();
        rusLocale = new Locale("ru");
        LogHelper.d(TAG, "new ApiManager Created");
    }

    @Override
    public synchronized List<GeoCache> getGeoCacheList(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
        LogHelper.d(TAG, "getGeoCacheList");

        double maxLatitude = (double) upperLeftCorner.getLatitudeE6() / 1E6;
        double minLatitude = (double) lowerRightCorner.getLatitudeE6() / 1E6;
        double maxLongitude = (double) lowerRightCorner.getLongitudeE6() / 1E6;
        double minLongitude = (double) upperLeftCorner.getLongitudeE6() / 1E6;

        if (maxLatitude == minLatitude && maxLongitude == minLongitude) {
            LogHelper.d(TAG, "Size of obtained listGeoCaches: 0");
            return filterGeoCaches(maxLatitude, minLatitude, maxLongitude, minLongitude);
        }

        GeoCacheSaxHandler handler = null;
        HttpURLConnection connection = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            URL url = generateUrl(maxLatitude, minLatitude, maxLongitude, minLongitude);
            connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LogHelper.e(TAG, "Can't connect to geocaching.su. Response: " + connection.getResponseCode());
            }

            InputSource geoCacheXml = new InputSource(new InputStreamReader(connection.getInputStream(), ENCODING));
            handler = new GeoCacheSaxHandler();
            parser.parse(geoCacheXml, handler);
            geoCaches.addAll(handler.getGeoCaches());
        } catch (MalformedURLException e) {
            LogHelper.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            LogHelper.e(TAG, e.getMessage(), e);
        } catch (SAXException e) {
            LogHelper.e(TAG, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LogHelper.e(TAG, e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        LogHelper.d(TAG, "Size of obtained listGeoCaches: " + geoCaches.size());
        return filterGeoCaches(maxLatitude, minLatitude, maxLongitude, minLongitude);
    }

    private URL generateUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
        String request = String.format(rusLocale, "%s?lngmax=%f&lngmin=%f&latmax=%f&latmin=%f&id=%d&geocaching=5767e405a17c4b0e1cbaecffdb93475d", URL, maxLongitude, minLongitude, maxLatitude,
            minLatitude, id);
        LogHelper.d(TAG, "generated Url: " + request);
        return new URL(request);
    }

    private List<GeoCache> filterGeoCaches(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
        List<GeoCache> filteredGeoCaches = new LinkedList<GeoCache>();
        GeoPoint gp;
        for (GeoCache gc : geoCaches) {
            gp = gc.getLocationGeoPoint();
            if (gp.getLatitudeE6() < maxLatitude * 1E6 && gp.getLatitudeE6() > minLatitude * 1E6 && gp.getLongitudeE6() < maxLongitude * 1e6 && gp.getLongitudeE6() > minLongitude * 1e6) {
                filteredGeoCaches.add(gc);
            }
        }
        LogHelper.d(TAG, "filterGeoCaches: " + filteredGeoCaches.size());
        return filteredGeoCaches;
    }

}
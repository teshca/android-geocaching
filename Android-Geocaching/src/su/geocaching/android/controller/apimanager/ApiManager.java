package su.geocaching.android.controller.apimanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.InfoActivity;
import su.geocaching.android.ui.InfoActivity.PageState;
import su.geocaching.android.ui.R;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

/**
 * Class for getting data from Geocaching.su. This class implements IApiManager
 * 
 * @author Nikita Bumakov
 */
public class ApiManager implements IApiManager {

    public static final String UTF8_ENCODING = "UTF-8";
    public static final String CP1251_ENCODING = "windows-1251";
    public static final String LINK_INFO_CACHE = "http://pda.geocaching.su/cache.php?cid=%d&mode=0";
    public static final String LINK_NOTEBOOK_TEXT = "http://pda.geocaching.su/note.php?cid=%d&mode=0";
    public static final String LINK_PHOTO_PAGE = "http://pda.geocaching.su/pict.php?cid=%d&mode=0";
    public static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
    private static final String LINK_GEOCACHE_LIST = "http://www.geocaching.su/pages/1031.ajax.php?lngmax=%f&lngmin=%f&latmax=%f&latmin=%f&id=%d&geocaching=5767e405a17c4b0e1cbaecffdb93475d";

    private static final String TAG = ApiManager.class.getCanonicalName();
    private static final Locale rusLocale = new Locale("ru");

    private int id;
    private HashSet<GeoCache> geoCaches = new HashSet<GeoCache>();
    private AsyncTask<Void, Void, String> downloadInfoTask;

    public ApiManager() {
        id = (int) (Math.random() * 1E7);
        LogManager.d(TAG, "new ApiManager Created");
    }

    @Override
    public synchronized List<GeoCache> getGeoCacheList(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
        LogManager.d(TAG, "getGeoCacheList");

        double maxLatitude = (double) upperLeftCorner.getLatitudeE6() / 1E6;
        double minLatitude = (double) lowerRightCorner.getLatitudeE6() / 1E6;
        double maxLongitude = (double) lowerRightCorner.getLongitudeE6() / 1E6;
        double minLongitude = (double) upperLeftCorner.getLongitudeE6() / 1E6;

        if (!Controller.getInstance().getConnectionManager().isInternetConnected()) {
            return filterGeoCaches(maxLatitude, minLatitude, maxLongitude, minLongitude);
        }

        if (maxLatitude == minLatitude && maxLongitude == minLongitude) {
            LogManager.d(TAG, "Size of obtained listGeoCaches: 0");
            return new LinkedList<GeoCache>();
        }

        GeoCachesSaxHandler handler;
        HttpURLConnection connection = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            URL url = generateUrl(maxLatitude, minLatitude, maxLongitude, minLongitude);
            connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LogManager.e(TAG, "Can't connect to geocaching.su. Response: " + connection.getResponseCode());
                return filterGeoCaches(maxLatitude, minLatitude, maxLongitude, minLongitude);
            }

            InputSource geoCacheXml = new InputSource(new InputStreamReader(connection.getInputStream(), CP1251_ENCODING));
            handler = new GeoCachesSaxHandler();
            parser.parse(geoCacheXml, handler);
            geoCaches.addAll(handler.getGeoCaches());
        } catch (MalformedURLException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (SAXException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        LogManager.d(TAG, "Size of obtained listGeoCaches: " + geoCaches.size());
        return filterGeoCaches(maxLatitude, minLatitude, maxLongitude, minLongitude);
    }

    private URL generateUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
        String request = String.format(rusLocale, LINK_GEOCACHE_LIST, maxLongitude, minLongitude, maxLatitude, minLatitude, id);
        LogManager.d(TAG, "generated Url: " + request);
        return new URL(request);
    }

    private List<GeoCache> filterGeoCaches(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
        List<GeoCache> filteredGeoCaches = new LinkedList<GeoCache>();
        GeoPoint gp;
        for (GeoCache gc : geoCaches) {
            gp = gc.getLocationGeoPoint();
            if ((gp.getLatitudeE6() < maxLatitude * 1E6) && (gp.getLatitudeE6() > minLatitude * 1E6) && (gp.getLongitudeE6() < maxLongitude * 1e6) && (gp.getLongitudeE6() > minLongitude * 1e6)) {
                filteredGeoCaches.add(gc);
            }
        }
        LogManager.d(TAG, "filterGeoCaches: " + filteredGeoCaches.size());
        return filteredGeoCaches;
    }

    @Override
    public void downloadInfo(Context context, DownloadInfoState state, InfoActivity infoActivity, int cacheId) {
        if (downloadInfoTask != null) {
            downloadInfoTask.cancel(false); // TODO check it
        }
        downloadInfoTask = new DownloadInfoTask(context, cacheId, infoActivity, state);
        downloadInfoTask.execute();

    }

    @Override
    public void downloadPhotos(Context context, PageState type, InfoActivity infoActivity, int cacheId) {
        
        if (!Controller.getInstance().getConnectionManager().isInternetConnected()) {
            Toast.makeText(context, context.getString(R.string.no_internet), Toast.LENGTH_LONG).show();
            return;
        }

        String HtmlWithPhotoLinks = "";
        try {
            HtmlWithPhotoLinks = new DownloadInfoTask(null, cacheId, infoActivity, DownloadInfoState.DOWNLOAD_PHOTO_PAGE).execute().get();
        } catch (InterruptedException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, e.getMessage(), e);
        }
        Pattern linkPattern = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
        Matcher pageMatcher = linkPattern.matcher(HtmlWithPhotoLinks);
        ArrayList<String> links = new ArrayList<String>();
        while (pageMatcher.find()) {
            links.add(pageMatcher.group());
        }

        List<URL> photoUrls = new ArrayList<URL>();
        for (String url : links) {
            try {
                String photoLink = url.substring(7, url.length() - 1);
                if (photoLink.endsWith(".jpg")) {
                    photoUrls.add(new URL(photoLink));
                }
            } catch (MalformedURLException e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
        new DownloadPhotoTask(context, cacheId).execute(photoUrls.toArray(new URL[photoUrls.size()]));
    }
}
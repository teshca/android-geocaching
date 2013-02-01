package su.geocaching.android.controller.apimanager;

import android.net.http.AndroidHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Class for getting data from geocaching.su. This class implements IApiManager
 *
 * @author Nikita Bumakov
 */
public class GeocachingSuApiManager implements IApiManager {

    private static final String TAG = GeocachingSuApiManager.class.getCanonicalName();
   
    public static final String LINK_INFO_TEXT = "http://pda.geocaching.su/cache.php?cid=%d&mode=0";
    public static final String LINK_NOTEBOOK_TEXT = "http://pda.geocaching.su/note.php?cid=%d&mode=0";
    public static final String LINK_PHOTO_PAGE = "http://pda.geocaching.su/pict.php?cid=%d&mode=0";
    public static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
    private static final String LINK_GEOCACHE_LIST = "http://www.geocaching.su/pages/1031.ajax.php?lngmax=%f&lngmin=%f&latmax=%f&latmin=%f&id=%d&geocaching=5767e405a17c4b0e1cbaecffdb93475d&exactly=1";

    private int id;
    private GeoCacheMemoryStorage memoryStorage;

    public GeocachingSuApiManager() {
        id = (int) (Math.random() * 1E7);
        memoryStorage = new GeoCacheMemoryStorage();
        LogManager.d(TAG, "new GeocachingSuApiManager Created");
    }

    @Override
    public synchronized List<GeoCache> getGeoCacheList(GeoRect rect) {
        LogManager.d(TAG, "getGeoCacheList");

        if (!Controller.getInstance().getConnectionManager().isActiveNetworkConnected() ||
                memoryStorage.isRectangleStored(rect)) {
            LogManager.d(TAG, "Get response from cache");
            return memoryStorage.getCaches(rect);
        }

        final GeoCachesSaxHandler handler;
        InputStreamReader inputStreamReader = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            double maxLatitude = rect.tl.getLatitude();
            double minLatitude = rect.br.getLatitude();
            double maxLongitude = rect.br.getLongitude();
            double minLongitude = rect.tl.getLongitude();
            URL url = getCacheListUrl(maxLatitude, minLatitude, maxLongitude, minLongitude);

            inputStreamReader = getInputSteamReader(url);
            InputSource geoCacheXml = new InputSource(inputStreamReader);
            handler = new GeoCachesSaxHandler();
            parser.parse(geoCacheXml, handler);
            memoryStorage.addCaches(handler.getGeoCaches(), rect);
        } catch (MalformedURLException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (SAXException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    LogManager.e(TAG, e.getMessage(), e);
                }
            }
        }

        return memoryStorage.getCaches(rect);
    }

    private URL getCacheListUrl(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) throws MalformedURLException {
        String request = String.format(Locale.ENGLISH, LINK_GEOCACHE_LIST, maxLongitude, minLongitude, maxLatitude, minLatitude, id);
        LogManager.d(TAG, "generated Url: " + request);
        return new URL(request);
    }

    @Override
    public String getNotebook(int cacheId) {
        try {
            return getText(getNotebookUrl(cacheId));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    private URL getNotebookUrl(int cacheId) throws MalformedURLException {
        return new URL(String.format(LINK_NOTEBOOK_TEXT, cacheId));
    }      
    
    @Override
    public String getInfo(int cacheId) {
        try {
            return getText(getInfoUrl(cacheId));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    private URL getInfoUrl(int cacheId) throws MalformedURLException {
        return new URL(String.format(LINK_INFO_TEXT, cacheId));
    }
    
    @Override
    public List<URL> getPhotoList(int cacheId) {
        try {
            String photoPage = getText(getPhotoUrl(cacheId));
            if (photoPage == null) return null;
            
            Pattern linkPattern = Pattern.compile("<a href=\"([^>]*)\"><img [^>]*></a>");
            Matcher pageMatcher = linkPattern.matcher(photoPage);
            ArrayList<String> links = new ArrayList<String>();
            while (pageMatcher.find()) {
                links.add(pageMatcher.group(1));
            }

            List<URL> photoUrls = new ArrayList<URL>();
            for (String photoLink : links) {
                try {
                    photoUrls.add(new URL(photoLink));                
                } catch (MalformedURLException e) {
                    LogManager.e(TAG, e.getMessage(), e);
                }
            }
            
            return photoUrls;
        } catch (MalformedURLException e) {
            return null;
        }
    }  

    private URL getPhotoUrl(int cacheId) throws MalformedURLException {
        return new URL(String.format(LINK_PHOTO_PAGE, cacheId));
    }    
    
    private String getText(URL url) {
        String result = null;

        if (Controller.getInstance().getConnectionManager().isActiveNetworkConnected()) {
            boolean success = false;
            for (int attempt = 0; attempt < 5 && !success; attempt++)
                try {
                    result = downloadText(url);
                    success = true;
                } catch (IOException e) {
                    // result is null in this case
                    LogManager.e(TAG, "getInfo failed", e);
                }
        }
        
        return result;
    }
    
    private String downloadText(URL url) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader in = null;
        try {
            InputStreamReader inputStreamReader = getInputSteamReader(url);
            in = new BufferedReader(inputStreamReader);
            int size;
            while ((size = in.read(buffer)) != -1) {
                html.append(buffer, 0, size);
            }                    
        } finally {
            if (in != null) {
                in.close();   
            }
        }

        String resultHtml = html.toString();
        resultHtml = resultHtml.replace(CP1251_ENCODING, UTF8_ENCODING);
        resultHtml = resultHtml.replaceAll("\\r|\\n", "");
        
        return resultHtml;
    }

    private InputStreamReader getInputSteamReader(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip;q=1.0, identity;q=0.5, *;q=0");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Can't connect to geocaching.su. Response: " + connection.getResponseCode());
        }
        InputStream inputStream = connection.getInputStream();
        if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
            inputStream = new GZIPInputStream(inputStream);
        }
        String charset = getCharsetFromContentType(connection.getContentType());

        return new InputStreamReader(inputStream, charset);
    }

    private String getCharsetFromContentType(String contentType) {
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.toLowerCase().startsWith("charset=")) {
                    return param.split("=", 2)[1];
                }
            }
        }
        return CP1251_ENCODING;
    }

    @Override
    public Boolean downloadPhoto(int cacheId, URL photoUrl) {
        String fileName = photoUrl.getPath().substring(photoUrl.getPath().lastIndexOf("/"));
        File file = Controller.getInstance().getExternalStorageManager().getPhotoFile(fileName, cacheId);
        boolean success = false;
        for (int attempt = 0; attempt < 5 && !success; attempt++)
            try {
                success = downloadAndSavePhoto(photoUrl, file);
            } catch (IOException e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        return success;
    } 
    
    private boolean downloadAndSavePhoto(URL url, File file) throws IOException {

        if (file == null) {
            LogManager.e(TAG, "file is null");
            return false;
        }
        
        if (url == null) {
            LogManager.e(TAG, "url is null");
            return false;
        }
                
        if (!Controller.getInstance().getConnectionManager().isActiveNetworkConnected()) {
            return false;
        }

        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url.toString());  //TODO overhead
        OutputStream outputStream = null;

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LogManager.w(TAG, String.format("Error %d while retrieving bitmap from %s",  statusCode, url));
                return false;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    inputStream = entity.getContent();
                    int size;
                    byte[] buffer = new byte[1024];
                    while ((size = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, size);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            } else {
                LogManager.e(TAG, String.format("Error while retrieving bitmap from %s. Content is null", url));
                return false;
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            LogManager.e(TAG,  String.format("Error while retrieving bitmap from %s", url), e);
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return true;
    }
}
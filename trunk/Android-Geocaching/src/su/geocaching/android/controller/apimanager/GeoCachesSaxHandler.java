package su.geocaching.android.controller.apimanager;

import com.google.android.maps.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;

import java.util.LinkedList;
import java.util.List;

/**
 * Class for parsing data from geocaching.su and put it in the List of GeoCache. Parse XML file is as follows:
 * <p/>
 * <p/>
 * 
 * <pre>
 *         {@code
 *         <c>
 *             <id>8901</id>
 *             <cn>10</cn>
 *             <a>47</a>
 *             <n>Geocache name</n>
 *             <la>59.6952333333</la>
 *             <ln>29.3968666667</ln>
 *             <ct>3</ct>
 *             <st>1</st>
 *         </c>
 *         }
 * </pre>
 * 
 * @author Nikita Bumakov
 */
public class GeoCachesSaxHandler extends DefaultHandler {

    private static final String TAG = GeoCachesSaxHandler.class.getCanonicalName();

    private final static String C = "c";
    private final static String ID = "id";
    private final static String CN = "cn";
    private final static String AREA = "a";
    private final static String NAME = "n";
    private final static String LATITUDE = "la";
    private final static String LONGITUDE = "ln";
    private final static String CACHE_TYPE = "ct";
    private final static String STATUS = "st";

    private GeoCache geoCache;
    private int latitude, longitude;
    private String text;
    private LinkedList<GeoCache> geoCacheList;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        text += new String(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void startDocument() throws SAXException {
        geoCacheList = new LinkedList<GeoCache>();
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (localName.equalsIgnoreCase(C)) {
            geoCache = new GeoCache();
        } else {
            text = "";
        }
        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (text != null) {
            text.trim();
        }

        if (localName.equalsIgnoreCase(ID)) {
            int id = parseInt(text, 0);
            geoCache.setId(id);
        } else if (localName.equalsIgnoreCase(CN)) {
            // TODO What is CN?
        } else if (localName.equalsIgnoreCase(AREA)) {
        } else if (localName.equalsIgnoreCase(NAME)) {
            geoCache.setName(text);
        } else if (localName.equalsIgnoreCase(LATITUDE)) {
            latitude = parseCoordinate(text);
        } else if (localName.equalsIgnoreCase(LONGITUDE)) {
            longitude = parseCoordinate(text);
        } else if (localName.equalsIgnoreCase(CACHE_TYPE)) {
            int type = parseCacheParameter(text);
            setGeoCacheType(type);
        } else if (localName.equalsIgnoreCase(STATUS)) {
            int status = parseCacheParameter(text);
            setGeoCacheStatus(status);
        } else if (localName.equalsIgnoreCase(C)) {
            geoCache.setLocationGeoPoint(new GeoPoint(latitude, longitude));
            geoCacheList.add(geoCache);
        }

        super.endElement(uri, localName, qName);
    }

    private static int parseCoordinate(String coordinate) {
        int result = 0;
        try {
            result = (int) (Double.parseDouble(coordinate) * 1E6);
        } catch (NumberFormatException e) {
            LogManager.e(TAG, "parseCoordinate: Invalid numeric format", e);
        }
        return result;
    }

    private static int parseCacheParameter(String parameter) {
        return parseInt(parameter, 1);
    }

    private static int parseInt(String number, int defaultValue) {
        try {
            defaultValue = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            LogManager.e(TAG, "parseInt: Invalid numeric format", e);
        }
        return defaultValue;
    }

    private void setGeoCacheType(int type) {
        switch (type) {
            case 1:
                geoCache.setType(GeoCacheType.TRADITIONAL);
                break;
            case 2:
                geoCache.setType(GeoCacheType.STEP_BY_STEP_TRADITIONAL);
                break;
            case 3:
                geoCache.setType(GeoCacheType.VIRTUAL);
                break;
            case 4:
                geoCache.setType(GeoCacheType.EVENT);
                break;
            case 5:
                geoCache.setType(GeoCacheType.WEBCAM);
                break;
            case 6:
                geoCache.setType(GeoCacheType.EXTREME);
                break;
            case 7:
                geoCache.setType(GeoCacheType.STEP_BY_STEP_VIRTUAL);
                break;
            case 8:
                geoCache.setType(GeoCacheType.CONTEST);
                break;
            default:
                geoCache.setType(GeoCacheType.TRADITIONAL);
                break;
        }
    }

    private void setGeoCacheStatus(int status) {
        switch (status) {
            case 1:
                geoCache.setStatus(GeoCacheStatus.VALID);
                break;
            case 2:
                geoCache.setStatus(GeoCacheStatus.NOT_VALID);
                break;
            case 3:
                geoCache.setStatus(GeoCacheStatus.NOT_CONFIRMED);
                break;
            default:
                geoCache.setStatus(GeoCacheStatus.VALID);
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * @return LinkedList obtained geocaches
     */
    public List<GeoCache> getGeoCaches() {
        return geoCacheList;
    }
}
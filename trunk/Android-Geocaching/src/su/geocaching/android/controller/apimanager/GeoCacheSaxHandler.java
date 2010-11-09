package su.geocaching.android.controller.apimanager;

import com.google.android.maps.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;

import java.util.LinkedList;

/*
 * <c>
 * <id>8901</id>
 * <cn>10</cn>
 * <a>47</a>
 * <n>Троицкая церковь в селе Мёдуши</n>
 * <la>59.6952333333</la>
 * <ln>29.3968666667</ln>
 * <ct>3</ct>
 * <st>1</st>
 * </c>
 */

/**
 * @author Nikita
 *         <p/>
 *         Class for parsing data from geocaching.su and put it in the List of
 *         GeoCach. This class extends DefaultHandler
 */
public class GeoCacheSaxHandler extends DefaultHandler {

	private final static String C = "c";
	private final static String ID = "id";
	private final static String CN = "cn";
	private final static String A = "a";
	private final static String NAME = "n";
	private final static String LATITUDE = "la";
	private final static String LONGITUDE = "ln";
	private final static String CACH_TYPE = "ct";
	private final static String STATUS = "st";

	private GeoCache geoCache;
	private int latitude, longitude;
	private String text;
	private LinkedList<GeoCache> geoCacheList;

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		text = new String(ch, start, length).trim();
		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equalsIgnoreCase(ID)) {
			geoCache.setId(Integer.parseInt(text));
		} else if (localName.equalsIgnoreCase(CN)) {
			// TODO What is CN?
		} else if (localName.equalsIgnoreCase(A)) {
			// TODO What is A?
		} else if (localName.equalsIgnoreCase(NAME)) {
			geoCache.setName(text);
		} else if (localName.equalsIgnoreCase(LATITUDE)) {
			latitude = (int) (Double.parseDouble(text) * 1E6);
		} else if (localName.equalsIgnoreCase(LONGITUDE)) {
			longitude = (int) (Double.parseDouble(text) * 1E6);
			geoCache.setLocationGeoPoint(new GeoPoint(latitude, longitude));
		} else if (localName.equalsIgnoreCase(CACH_TYPE)) {
			int type = Integer.parseInt(text);
			setGeoCacheType(type);
		} else if (localName.equalsIgnoreCase(STATUS)) {
			int status = Integer.parseInt(text);
			setGeoCacheStatus(status);
		} else if (localName.equalsIgnoreCase(C)) {
			geoCacheList.add(geoCache);
		}

		if (localName.equalsIgnoreCase(ID)) {
			geoCache.setId(Integer.parseInt(text));
		} else if (localName.equalsIgnoreCase(CN)) {
			// TODO What is CN?
		} else if (localName.equalsIgnoreCase(A)) {
			// TODO What is A?
		} else if (localName.equalsIgnoreCase(NAME)) {
			geoCache.setName(text);
		} else if (localName.equalsIgnoreCase(LATITUDE)) {
			latitude = (int) (Double.parseDouble(text) * 1E6);
		} else if (localName.equalsIgnoreCase(LONGITUDE)) {
			longitude = (int) (Double.parseDouble(text) * 1E6);
			geoCache.setLocationGeoPoint(new GeoPoint(latitude, longitude));
		} else if (localName.equalsIgnoreCase(CACH_TYPE)) {
			int type = Integer.parseInt(text);
			setGeoCacheType(type);
		} else if (localName.equalsIgnoreCase(STATUS)) {
			int status = Integer.parseInt(text);
			setGeoCacheStatus(status);
		} else if (localName.equalsIgnoreCase(C)) {
			geoCacheList.add(geoCache);
		}

		super.endElement(uri, localName, qName);
	}

	private void setGeoCacheType(int type) {
		switch (type) {
		case 1:
			geoCache.setType(GeoCacheType.TRADITIONAL);
			break;
		case 2:
			geoCache.setType(GeoCacheType.STEP_BY_STEP);
			break;
		case 3:
			geoCache.setType(GeoCacheType.VIRTUAL);
			break;
		case 4:
			geoCache.setType(GeoCacheType.EVENT);
			break;
		case 6:
			geoCache.setType(GeoCacheType.EXTREME);
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
		}
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
		}
	}

	public LinkedList<GeoCache> getGeoCaches() {
		return geoCacheList;
	}
}

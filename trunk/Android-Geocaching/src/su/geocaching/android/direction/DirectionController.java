package su.geocaching.android.direction;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.PatternSyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class DirectionController {
    private GeoPoint userGeoPoint;
    private GeoPoint cacheGeoPoint;
    private MapView mapView;
    private boolean visible;

    public DirectionController(GeoPoint userGeoPoint, GeoPoint cacheGeoPoint, MapView map) {
	this.userGeoPoint = userGeoPoint;
	this.cacheGeoPoint = cacheGeoPoint;
	mapView = map;
	visible = true;
    }

    /**
     * creates the path according to the received points
     * **/

    public void getDirectionPath(GeoPoint userPoint, GeoPoint cachePoint, int color) {
	if (visible) {
	 
	    String origin = Double.toString((double) userPoint.getLatitudeE6() / 1.0E6) + "," + Double.toString((double) userPoint.getLongitudeE6() / 1.0E6);
	    String end = Double.toString((double) cachePoint.getLatitudeE6() / 1.0E6) + "," + Double.toString((double) cachePoint.getLongitudeE6() / 1.0E6);
	    String urlString = "http://maps.google.com/maps?f=d&hl=en&saddr=" + origin + "&daddr=" + end + "&ie=UTF8&0&om=0&output=kml";
	    Log.d("URL", urlString);
	    Document doc = null;
	    HttpURLConnection urlConnection = null;
	    URL url = null;
	    String pathConent = "";
	    try {
		try {
		    try {
			try {
			    try {
				try {
				    url = new URL(urlString.toString());
				    urlConnection = (HttpURLConnection) url.openConnection();
				    urlConnection.setRequestMethod("GET");
				    urlConnection.setDoOutput(true);

				    urlConnection.setDoInput(true);
				    urlConnection.connect();
				    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				    DocumentBuilder db = dbf.newDocumentBuilder();
				    doc = db.parse(urlConnection.getInputStream());
				    Log.d("Document", "is created");
				} catch (ParserConfigurationException e) {
				    Log.d("ParserConfigurationException", "problem in doc.builderfactory");
				}
			    } catch (SAXException e) {
				Log.d("SAXException", "problem in doc.builder");
			    }
			} catch (IllegalAccessError e) {
			    Log.d("IllegalAccessError", "setDoInput of urlConnecttion works wrong");
			}

		    } catch (MalformedURLException e) {
			Log.d("MalformedURLException", "problem in doc.builder");
		    }
		} catch (ProtocolException e) {
		    Log.d("ProtocolException", "setRequestMethod of urlConnecttion works wrong");
		}

	    } catch (IOException e) {
		Log.d("IOException", "problem in input or output stream");
	    }

	    if (doc.getElementsByTagName("GeometryCollection").getLength() > 0) {
		// String path =
		// doc.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getNodeName();
		String path = doc.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getFirstChild().getNodeValue();

		String[] pairs = path.split(" ");
		String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude
						       // lngLat[1]=latitude
						       // lngLat[2]=height
		// src
		GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
		mapView.getOverlays().add(new DirectionPathOverlay(startGP, startGP, 1));
		GeoPoint gp1;
		GeoPoint gp2 = startGP;
		for (int i = 1; i < pairs.length; i++) // the last one would be
						       // crash
		{
		    lngLat = pairs[i].split(",");
		    gp1 = gp2;
		    // watch out! For GeoPoint, first:latitude, second:longitude
		    gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
		    mapView.getOverlays().add(new DirectionPathOverlay(gp1, gp2, 2));
		}
		mapView.getOverlays().add(new DirectionPathOverlay(cachePoint, cachePoint, 3));
	    }

	}
    }

    /**
     * sent a specified message to Google service,and receive a *.kml file
     * ,which contains points for path realization,which were encoded , and
     * decodes them
     **/

    public boolean isVisible() {
	return visible;
    }

    public void setVisibleWay() {
	this.visible = !visible;
    }

    public void setCachePoint(GeoPoint cachePoint) {
	this.cacheGeoPoint = cachePoint;
    }

    public void setUserPoint(GeoPoint userPoint) {
	this.userGeoPoint = userPoint;
    }
}
/*
 * System.out.println(origin); System.out.println(end); String pairs[] =
 * getDirectionData(origin, end);
 * 
 * if (pairs != null) { String[] lngLat = pairs[0].split(",");
 * 
 * // STARTING POINT GeoPoint startGP = new GeoPoint((int)
 * (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) *
 * 1E6));
 * 
 * userPoint = startGP; // mapController.setCenter(userPoint); //
 * mapController.setZoom(15); mapView.getOverlays().add(new
 * DirectionPathOverlay(startGP, startGP));
 * 
 * // NAVIGATE THE PATH GeoPoint gp1; GeoPoint gp2 = startGP;
 * 
 * for (int i = 1; i < pairs.length; i++) { lngLat = pairs[i].split(","); gp1 =
 * gp2; // watch out! For GeoPoint, first:latitude, second:longitude gp2 = new
 * GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int)
 * (Double.parseDouble(lngLat[0]) * 1E6)); mapView.getOverlays().add(new
 * DirectionPathOverlay(gp1, gp2));
 * 
 * }
 * 
 * // END POINT mapView.getOverlays().add(new DirectionPathOverlay(gp2, gp2));
 * // map.getController().animateTo(startGP);
 */
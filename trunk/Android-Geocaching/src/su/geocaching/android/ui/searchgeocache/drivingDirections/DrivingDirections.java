package su.geocaching.android.ui.searchgeocache.drivingDirections;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class DrivingDirections {
	private GeoPoint startPoint, endPoint;
	private MapView map;

	public DrivingDirections(GeoPoint startPoint, GeoPoint endPoint) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	public boolean drawWay(MapView map) {
		this.map = map;
		double flat = startPoint.getLatitudeE6() / 1.0E6, flong = startPoint.getLongitudeE6() / 1.0E6;

		String test = "http://maps.google.ru/maps?f=d&source=s_d&saddr=" + flat + "," + flong + "&daddr=" + Double.toString((double) endPoint.getLatitudeE6() / 1.0E6) + ","
				+ Double.toString((double) endPoint.getLongitudeE6() / 1.0E6) + "&geocode=&hl=ru&mra=ls&dirflg=w&vps=4&output=kml";
		// String test1 = "http://maps.google.ru/maps?f=d&source=s_d&saddr=" + startPoint.getLatitudeE6() / 1.0E6 + "," + startPoint.getLongitudeE6() / 1.0E6 + "&daddr=" + endPoint.getLatitudeE6()
		// / 1.0E6 + "," + endPoint.getLongitudeE6() / 1.0E6 + "&hl=ru&mra=ls&dirflg=w&sll=" + startPoint.getLatitudeE6() / 1.0E6 + "," + startPoint.getLongitudeE6() / 1.0E6
		// + "&ie=UTF8&0&om=0&output=kml";

		try {

			URL url = new URL(test);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.connect();

			// Log.d("inputstream", convertStreamToString(urlConnection.getInputStream()));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document document = builder.parse(urlConnection.getInputStream());
			if (document.getElementsByTagName("GeometryCollection").getLength() > 0) {
				// String path = doc.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getNodeName();
				String path = document.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getFirstChild().getNodeValue();
				Log.d("xxx", "path=" + path);
				String[] pairs = path.split(" ");
				String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude lngLat[1]=latitude lngLat[2]=height
				// src
				GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));

				GeoPoint gp1;
				GeoPoint gp2 = startGP;
				for (int i = 1; i < pairs.length; i++) // the last one would be crash
				{
					lngLat = pairs[i].split(",");
					gp1 = gp2;
					// watch out! For GeoPoint, first:latitude, second:longitude
					gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
					map.getOverlays().add(new DrivingDirectionsOverlay(gp1, gp2, 2, 3));
					Log.d("xxx", "pair:" + pairs[i]);
				}
				map.getOverlays().add(new DrivingDirectionsOverlay(endPoint, endPoint, 3)); // use the default color
			}
		} catch (ParserConfigurationException e) {
			Log.d("ParserConfigurationException", "problem on DocumentBuilder ");
			return false;
		} catch (IOException e) {
			Log.d("IOException", "problem from 105-111 ");
			return false;
		} catch (SAXException e) {
			Log.d("SAXException", "problem on *.parse(inputstream))");

			e.printStackTrace();
			return false;
		}

		return true;
	}
}
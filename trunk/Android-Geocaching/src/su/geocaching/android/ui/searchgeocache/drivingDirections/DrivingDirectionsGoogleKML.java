package su.geocaching.android.ui.searchgeocache.drivingDirections;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.ProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * Implementation of DrivingDirections that connects to the Google Maps web
 * service to download and parse a KML file containing the directions from one
 * geographical point to another.
 * 
 */
public abstract class DrivingDirectionsGoogleKML extends DrivingDirections {
	@Override
	protected void startDrivingTo(GeoPoint startPoint, GeoPoint endPoint,
			Mode mode, IDirectionsListener listener) {
		new LoadDirectionsTask(startPoint, endPoint).execute(mode);
	}

	private class LoadDirectionsTask extends AsyncTask<Mode, Void, RouteImpl> {
		private static final String BASE_URL = "http://maps.google.com/maps?f=d&hl=en";
		private static final String ELEMENT_PLACEMARK = "Placemark";
		private static final String ELEMENT_NAME = "name";
		private static final String ELEMENT_DESC = "description";
		private static final String ELEMENT_POINT = "Point";
		private static final String ELEMENT_ROUTE = "Route";
		private static final String ELEMENT_GEOM = "GeometryCollection";
                private  StringBuilder urlString;
		private GeoPoint startPoint;
		private GeoPoint endPoint;

		public LoadDirectionsTask(GeoPoint startPoint, GeoPoint endPoint) {
			this.startPoint = startPoint;
			this.endPoint = endPoint;
		}

		@Override
		protected RouteImpl doInBackground(Mode... params) {
			// Connect to the Google Maps web service that will return a KML
			// string
			// containing the directions from one point to another.
			//
//			urlString = new StringBuilder();
//			urlString.append(BASE_URL).append("&saddr=").append(
//					startPoint.getLatitudeE6()/1.0E6).append(",").append(
//					startPoint.getLongitudeE6()/1.0E6 ).append("&daddr=").append(endPoint.getLatitudeE6()/1.0E6).append(",").append(endPoint.getLongitudeE6()/1.0E6)
//					.append(
//							"&ie=UTF8&0&om=0&output=kml");
//String s= "http://maps.googleapis.com/maps/api/directions/json?origin="+startPoint.getLatitudeE6()+","+startPoint.getLongitudeE6()+"&destination="+endPoint.getLatitudeE6()+","+endPoint.getLongitudeE6();
//			if (params[0] == Mode.WALKING) {
//				urlString.append("&dirflg=w");
//			}
//Log.d("FileMessage", urlString.toString());
			
String test ="http://maps.google.ru/maps?f=d&source=s_d&saddr=59.968096,+30.303894&daddr=59.97048,30.307971&geocode=&abauth=9b9a1b53:EL1rIvWygwh20UBkMw1SCKKS3gY&hl=ru&mra=ls&dirflg=w&vps=4&jsv=314b&sll=59.970085,30.306129&sspn=0.002218,0.016512&ie=UTF8&0&om=0&output=kml";
RouteImpl route = null;
			try {
				URL url = new URL(test);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.connect();
				
				 Log.d("inputstream", convertStreamToString(connection.getInputStream()));
				 route = parseResponse(connection.getInputStream());
			} catch (IllegalAccessError e) {
			    Log.d("IlligalaccessEror", "problem on *.setDoInOrOutput");
			} catch (MalformedURLException e) {
				Log.i("tag", "here");				e.printStackTrace();
			} catch (IOException e) {
Log.i("tag", "here");				e.printStackTrace();
			}
			return route;
		}

		private RouteImpl parseResponse(InputStream inputstream) {
			// Parse the KML file returned by the Google Maps web service
			// using the default XML DOM parser.
			//
			try {
			    
			  Log.d("inputstream", convertStreamToString(inputstream));
			        
			        
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				
				Document document = builder.parse(inputstream);
			
				NodeList placemarkList = document.getElementsByTagName(ELEMENT_PLACEMARK);
				
				// Get the list of placemarks to plot along the route.
				//
				List<IPlacemark> placemarks = new ArrayList<IPlacemark>();
				for (int i = 0; i < placemarkList.getLength(); i++) {
					PlacemarkImpl placemark = parsePlacemark(placemarkList
							.item(i));
					if (placemark != null) {
						placemarks.add(placemark);
					}
				}
Log.d("List of Placemarks", " "+placemarks.size());
				// Get the route defining the driving directions.
				//
				RouteImpl route = parseRoute(placemarkList);
				route.setPlacemarks(placemarks);

				return route;
			} catch (ParserConfigurationException e) {
			    Log.d("ParserConfigurationException", "problem on DocumentBuilder ");
			    return null;
			} catch (IOException e) {
			    Log.d("IOException", "problem from 105-111 ");
			    return null;
			} catch (SAXException e) {
			    Log.d("SAXException", "problem on *.parse(inputstream))");
			 
			    
			    e.printStackTrace();
			    return null;
			}

		}

		private PlacemarkImpl parsePlacemark(Node item) {
			PlacemarkImpl placemark = new PlacemarkImpl();

			boolean isRouteElement = false;
			NodeList children = item.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeName().equals(ELEMENT_NAME)) {
					// Get the value of the <name> KML tag.
					// If the value is "Route", this is not a placemark
					// description
					// but a route description.
					//
					String name = node.getFirstChild().getNodeValue();
					if (name.equals(ELEMENT_ROUTE)) {
						isRouteElement = true;
					} else {
						isRouteElement = false;
						placemark.setInstructions(name);
					}
				} else if (node.getNodeName().equals(ELEMENT_DESC)) {
					// Get the value of the <description> KML tag if it is a
					// placemark
					// that is being described (not a route).
					//
					if (!isRouteElement) {
						String distance = node.getFirstChild().getNodeValue();
						placemark.setDistance(distance.substring(3).replace(
								"&#160;", " "));
					}
				} else if (node.getNodeName().equals(ELEMENT_POINT)) {
					// Get the value of the <Point> coordinates KML tag if it is
					// a placemark
					// that is being described (not a route).
					//
					if (!isRouteElement) {
						String coords = node.getFirstChild().getFirstChild()
								.getNodeValue();
						String[] latlon = coords.split(",");
						placemark.setLocation(new GeoPoint((int) (Double
								.parseDouble(latlon[1]) * 1E6), (int) (Double
								.parseDouble(latlon[0]) * 1E6)));
					}
				}
			}

			return isRouteElement ? null : placemark;
		}

		private RouteImpl parseRoute(NodeList placemarkList) {
			RouteImpl route = null;

			for (int i = 0; i < placemarkList.getLength(); i++) {
				// Iterate through all the <Placemark> KML tags to find the one
				// whose child <name> tag is "Route".
				//
				Node item = placemarkList.item(i);
				NodeList children = item.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node node = children.item(j);
					if (node.getNodeName().equals(ELEMENT_NAME)) {
						String name = node.getFirstChild().getNodeValue();
						if (name.equals(ELEMENT_ROUTE)) {
							route = parseRoute(item);
							break;
						}
					}
				}
			}

			return route;
		}
		 public String convertStreamToString(InputStream is) throws IOException {
	        
	        if (is != null) {
	            Writer writer = new StringWriter();
                 StringBuilder str = new  StringBuilder();  
	            char[] buffer = new char[8*1024];
	            try {
	                Reader reader = new BufferedReader(
	                        new InputStreamReader(is, "UTF-8"),8*1024);
	                int n;
	                while ((n = reader.read(buffer)) != -1) {
	                    writer.write(buffer, 0, n);
	               str.append(writer.toString());
	               Log.i("str", str.toString());
	                }
	            } finally {
	               is.close();
	            }
	            return writer.toString();
	        } else {        
	            return "";
	        }
	    }
	
		private RouteImpl parseRoute(Node item) {
			RouteImpl route = new RouteImpl();

			NodeList children = item.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);

				if (node.getNodeName().equals(ELEMENT_DESC)) {
					// Get the value of the <description> KML tag.
					//
					String distance = node.getFirstChild().getNodeValue();
					route.setTotalDistance(distance.split("<br/>")[0]
							.substring(10).replace("&#160;", " "));
				} else if (node.getNodeName().equals(ELEMENT_GEOM)) {
					// Get the space-separated coordinates of the geographical
					// points defining the route.
					//
					String path = node.getFirstChild().getFirstChild()
							.getFirstChild().getNodeValue();
					String[] pairs = path.split(" ");

					// For each coordinate, get its {latitude, longitude} values
					// and add the corresponding
					// geographical point to the route.
					//
					List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
					for (int p = 0; p < pairs.length; p++) {
						String[] coords = pairs[p].split(",");
						GeoPoint geoPoint = new GeoPoint((int) (Double
								.parseDouble(coords[1]) * 1E6), (int) (Double
								.parseDouble(coords[0]) * 1E6));
						geoPoints.add(geoPoint);
						Log.i("T", " " + geoPoints.size());
					}
					route.setGeoPoints(geoPoints);
				}
			}

			return route;
		}

		protected void onPostExecute(RouteImpl route) {
			if (route == null) {
				DrivingDirectionsGoogleKML.this.onDirectionsNotAvailable();
			} else {
				DrivingDirectionsGoogleKML.this.onDirectionsAvailable(route);
			}
		}
	}
}
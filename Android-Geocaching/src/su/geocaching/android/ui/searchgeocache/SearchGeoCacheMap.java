package su.geocaching.android.ui.searchgeocache;

import java.util.List;

import su.geocaching.android.direction.DirectionController;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.*;
import su.geocaching.android.ui.geocachemap.ConnectionStateReceiver;
import su.geocaching.android.utils.Helper;
import su.geocaching.android.view.showgeocacheinfo.Info_cache;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.location.Location;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * @author Android-Geocaching.su student project team
 * @description Search GeoCache with the map.
 * @since October 2010
 */
public class SearchGeoCacheMap extends MapActivity implements ISearchActivity, IMapAware, IInternetAware {
    private GeoCacheOverlayItem cacheOverlayItem;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private UserLocationOverlay userOverlay;
    private TextView gpsStatusTextView;
    private TextView internetStatusTextView;
    private MapView map;
    private MapController mapController;
    private List<Overlay> mapOverlays;
    private SearchGeoCacheManager manager;
    private DirectionController directionControlller;
    private ConnectionStateReceiver internetManager;

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_geocache_map);
	gpsStatusTextView = (TextView) findViewById(R.id.gpsStatusTextView);
	internetStatusTextView = (TextView) findViewById(R.id.internetStatusTextView);
	map = (MapView) findViewById(R.id.searchGeocacheMap);
	mapOverlays = map.getOverlays();
	mapController = map.getController();
	userOverlay = new UserLocationOverlay(this, map);
	manager = new SearchGeoCacheManager(this);
	internetManager = new ConnectionStateReceiver(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onPause()
     */
    @Override
    protected void onPause() {
	super.onPause();
	manager.onPause();
	if (manager.isLocationFixed()) {
	    userOverlay.disableCompass();
	    userOverlay.disableMyLocation();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#onResume()
     */
    @Override
    protected void onResume() {
	super.onResume();
	manager.onResume();

	if (!internetManager.isInternetConnected()) {
	    onInternetLost();
	} else {
	    onInternetFound();
	}
    }

    /**
     * Init and run all activity content
     */
    @Override
    public void runLogic() {
	manager.runLogic();
	if (manager.getGeoCache() == null) {
	    return;
	}
	userOverlay.enableCompass();
	userOverlay.enableMyLocation();

	Drawable cacheMarker = this.getResources().getDrawable(R.drawable.orangecache);
	cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth(), 0);

	cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker, this);
	cacheOverlayItem = new GeoCacheOverlayItem(manager.getGeoCache(), "", "");
	cacheItemizedOverlay.addOverlayItem(cacheOverlayItem);
	mapOverlays.add(cacheItemizedOverlay);
	if (!manager.isLocationFixed()) {
	    mapController.animateTo(manager.getGeoCache().getLocationGeoPoint());
	}

	map.invalidate();
    }

    /**
     * Start SearchGeoCacheCompass activity
     */
    public void startCompassView() {
	Intent intent = new Intent(this, SearchGeoCacheCompass.class);
	if ((manager != null) && (manager.getGeoCache() != null)) {
	    intent.putExtra(GeoCache.class.getCanonicalName(), manager.getGeoCache());
	}
	if (manager != null) {
	    intent.putExtra("location fixed", manager.isLocationFixed());
	}
	startActivity(intent);
	this.finish();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateLocation
     * (android.location.Location)
     */
    @Override
    public void updateLocation(Location location) {
	userOverlay.onLocationChanged(location);
        
	if (distanceOverlay == null) {
	    // It's really first run of update location
	    resetZoom();
	    distanceOverlay = new DistanceToGeoCacheOverlay(Helper.locationToGeoPoint(location), manager.getGeoCache().getLocationGeoPoint());
	    distanceOverlay.setCachePoint(manager.getGeoCache().getLocationGeoPoint());
	    mapOverlays.add(distanceOverlay);
	    mapOverlays.add(userOverlay);
	    
	    directionControlller= new DirectionController(Helper.locationToGeoPoint(location), manager.getGeoCache().getLocationGeoPoint(), map);
	    directionControlller.getDirectionPath(Helper.locationToGeoPoint(location),  manager.getGeoCache().getLocationGeoPoint(),1);
	    
	    
	    return;
	}
	distanceOverlay.setUserPoint(Helper.locationToGeoPoint(location));

	map.invalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateAzimuth
     * (float)
     */
    @Override
    public void updateAzimuth(int bearing) {
	float[] values = new float[1];
	values[0] = bearing;
	userOverlay.onSensorChanged(Sensor.TYPE_ORIENTATION, values);
    }

    /**
     * Set map zoom which can show userPoint and GeoCachePoint
     */
    private void resetZoom() {
	GeoPoint currentGeoPoint = Helper.locationToGeoPoint(manager.getCurrentLocation());
	mapController.zoomToSpan(Math.abs(manager.getGeoCache().getLocationGeoPoint().getLatitudeE6() - currentGeoPoint.getLatitudeE6()), Math.abs(manager.getGeoCache().getLocationGeoPoint()
		.getLongitudeE6()
		- currentGeoPoint.getLongitudeE6()));

	GeoPoint center = new GeoPoint((manager.getGeoCache().getLocationGeoPoint().getLatitudeE6() + currentGeoPoint.getLatitudeE6()) / 2, (manager.getGeoCache().getLocationGeoPoint()
		.getLongitudeE6() + currentGeoPoint.getLongitudeE6()) / 2);
	mapController.animateTo(center);
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.search_geocache_map, menu);
	return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menuDefaultZoom:
	    if (manager.isLocationFixed()) {
		resetZoom();
	    }
	    return true;
	case R.id.menuStartCompass:
	    this.startCompassView();
	    return true;
	case R.id.menuToggleShortestWay:
	    distanceOverlay.toggleShorteshtWayVisible();
	    return true;
	case R.id.menuGeoCacheInfo:
	    manager.showGeoCacheInfo();
	    return true;
	case R.id.DrawDirectionPath:
             directionControlller.setVisibleWay();
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#updateStatus(
     * java.lang.String)
     */
    @Override
    public void updateStatus(String status, int type) {
	switch (type) {
	case ISearchActivity.STATUS_TYPE_GPS:
	    gpsStatusTextView.setText(status);
	    break;
	case ISearchActivity.STATUS_TYPE_INTERNET:
	    internetStatusTextView.setText(status);
	    break;
	default:
	    Toast.makeText(this, status, Toast.LENGTH_LONG);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.MapActivity#isRouteDisplayed()
     */
    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.ui.searchgeocache.ISearchActivity#getContext()
     */
    @Override
    public Context getContext() {
	return this.getBaseContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.searchgeocache.ISearchActivity#getLastKnownLocation
     * ()
     */
    @Override
    public Location getLastKnownLocation() {
	if (!manager.isLocationFixed()) {
	    return null;
	}
	return manager.getCurrentLocation();
    }

    /**
     * @return last known bearing
     */
    public int getLastKnownBearing() {
	return manager.getCurrentBearing();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.IMapAware#onGeoCacheItemTaped(su
     * .geocaching.android.ui.geocachemap.GeoCacheOverlayItem)
     */
    @Override
    public void onGeoCacheItemTaped(GeoCacheOverlayItem item) {
	Intent intent = new Intent(this, Info_cache.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), manager.getGeoCache());
	this.startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.ui.geocachemap.IInternetAware#onInternetLost()
     */
    @Override
    public void onInternetLost() {
	Toast.makeText(this, getString(R.string.search_geocache_internet_lost), Toast.LENGTH_LONG).show();
	updateStatus(getString(R.string.search_geocache_status_without_internet), ISearchActivity.STATUS_TYPE_INTERNET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * su.geocaching.android.ui.geocachemap.IInternetAware#onInternetFound()
     */
    @Override
    public void onInternetFound() {
	updateStatus(getString(R.string.search_geocache_status_with_internet), ISearchActivity.STATUS_TYPE_INTERNET);
    }

    /**
     * creates the path according to the received points
     * **/

    private void getDirectionPath(GeoPoint userPoint, GeoPoint cachePoint) {
//	String origin = Double.toString((double) userPoint.getLatitudeE6() / 1.0E6) + "," + Double.toString((double) userPoint.getLongitudeE6() / 1.0E6);
//	String end = Double.toString((double) cachePoint.getLatitudeE6() / 1.0E6) + "," + Double.toString((double) cachePoint.getLongitudeE6() / 1.0E6);
//	String pairs[] = getDirectionData(origin, end);
//
//	if (pairs != null) {
//	    String[] lngLat = pairs[0].split(",");
//
//	    // STARTING POINT
//	    GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
//
//	    userPoint = startGP;
//	    // mapController.setCenter(userPoint);
//	    // mapController.setZoom(15);
//	    map.getOverlays().add(new DirectionPathOverlay(startGP, startGP));
//
//	    // NAVIGATE THE PATH
//	    GeoPoint gp1;
//	    GeoPoint gp2 = startGP;
//
//	    for (int i = 1; i < pairs.length; i++) {
//		lngLat = pairs[i].split(",");
//		gp1 = gp2;
//		// watch out! For GeoPoint, first:latitude, second:longitude
//		gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
//		map.getOverlays().add(new DirectionPathOverlay(gp1, gp2));
//
//	    }
//
//	    // END POINT
//	    map.getOverlays().add(new DirectionPathOverlay(gp2, gp2));
//	    // map.getController().animateTo(startGP);
//	}
    }

    /**
     * sent a specified message to Google service,and receive a *.kml file
     * ,which contains points for path realization,which were encoded , and
     * decodes them
     **/
    private String[] getDirectionData(String srcPlace, String destPlace) {

//	String urlString = "http://maps.google.com/maps?f=d&hl=en&saddr=" + srcPlace + "&daddr=" + destPlace + "&ie=UTF8&0&om=0&output=kml";
//	Log.d("URL", urlString);
//	Document doc = null;
//	HttpURLConnection urlConnection = null;
//	URL url = null;
//	String pathConent = "";
//	try {
//	    try {
//		try {
//		    try {
//			try {
//			    try {
//				url = new URL(urlString.toString());
//				urlConnection = (HttpURLConnection) url.openConnection();
//				urlConnection.setRequestMethod("GET");
//				urlConnection.setDoOutput(true);
//
//				urlConnection.setDoInput(true);
//				urlConnection.connect();
//				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//				DocumentBuilder db = dbf.newDocumentBuilder();
//				doc = db.parse(urlConnection.getInputStream());
//				Log.d("Document", "is created");
//			    } catch (ParserConfigurationException e) {
//				Log.d("ParserConfigurationException", "problem in doc.builderfactory");
//			    }
//			} catch (SAXException e) {
//			    Log.d("SAXException", "problem in doc.builder");
//			}
//		    } catch (IllegalAccessError e) {
//			Log.d("IllegalAccessError", "setDoInput of urlConnecttion works wrong");
//		    }
//
//		} catch (MalformedURLException e) {
//		    Log.d("MalformedURLException", "problem in doc.builder");
//		}
//	    } catch (ProtocolException e) {
//		Log.d("ProtocolException", "setRequestMethod of urlConnecttion works wrong");
//	    }
//
//	} catch (IOException e) {
//	    Log.d("IOException", "problem in input or output stream");
//	}
//
//	NodeList nl = doc.getElementsByTagName("LineString");
//	for (int s = 0; s < nl.getLength(); s++) {
//	    Node rootNode = nl.item(s);
//	    NodeList configItems = rootNode.getChildNodes();
//	    for (int x = 0; x < configItems.getLength(); x++) {
//		Node lineStringNode = configItems.item(x);
//		NodeList path = lineStringNode.getChildNodes();
//		pathConent = path.item(0).getNodeValue();
//	    }
//	}
//	String[] tempContent = null;
//	try {
//	    try {
//		tempContent = pathConent.split(" ");
//
//	    } catch (NullPointerException e) {
//		Log.d("NullPointerException", "split's arg is null ");
//	    }
//
//	} catch (PatternSyntaxException e) {
//	    Log.d("PatternSyntaxException", "split's arg expresion isb't valid ");
//	}
//	return tempContent;
	return null;
    }
}
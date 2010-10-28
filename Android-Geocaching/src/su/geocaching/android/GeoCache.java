package su.geocaching.android;

import com.google.android.maps.GeoPoint;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	GeoCache - central concept of geocaching game.
 */
public class GeoCache {
    private GeoPoint location;	//Coordinates of cache location
    private int id;		//Unique identifier of GeoCache(from geocaching.su)

    // TODO: if cache id not found - throw exception?
    public GeoCache(int id) {
	this.id = id;

	// TODO: retrieve data from API Manager
	location = new GeoPoint(59879936, 2982861);
    }

    public GeoPoint getLocation() {
	return location;
    }

    public int getId() {
	return id;
    }
}

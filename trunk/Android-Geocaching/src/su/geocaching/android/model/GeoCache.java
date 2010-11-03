package su.geocaching.android.model;

import com.google.android.maps.GeoPoint;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	GeoCache - central concept of geocaching game.
 */
public class GeoCache {
    private GeoPoint locationGeoPoint;	//Coordinates of cache location
    private int id;			//Unique identifier of GeoCache(from geocaching.su)
    private String name;

    
    public GeoCache() {	
	locationGeoPoint = new GeoPoint(0, 0);
    }  
    
    // TODO: if cache id not found - throw exception?
    public GeoCache(int id) {
	this.id = id;

	// TODO: retrieve data from API Manager
	locationGeoPoint = new GeoPoint(59879936, 29828610);
    }     

    public GeoCache(int latitude, int longitude, int id) {
	this.id = id;	
	locationGeoPoint = new GeoPoint(latitude, longitude);
    }
    
    public GeoCache(int latitude, int longitude, int id, String name) {
	this.id = id;	
	locationGeoPoint = new GeoPoint(latitude, longitude);
    }    
    
    public GeoPoint getLocationGeoPoint() {
	return locationGeoPoint;
    }

    public int getId() {
	return id;
    }

    public String getName() {
        return name;
    }

    public void setLocationGeoPoint(GeoPoint locationGeoPoint) {
        this.locationGeoPoint = locationGeoPoint;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}

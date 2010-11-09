package su.geocaching.android.model.datatype;

import com.google.android.maps.GeoPoint;

/**
 * @author Nikita Bumakov
 * @since October 2010 GeoCache - central concept of geocaching game.
 */
public class GeoCache {
    private int id; // Unique identifier of GeoCache(from geocaching.su)
    private GeoPoint locationGeoPoint;   
    private String name;
    private GeoCacheType type;
    private GeoCacheStatus status;

    public GeoCache() {
	locationGeoPoint = new GeoPoint(0, 0);
    }

    @Deprecated
    public GeoCache(int id) {
	this.id = id;
	locationGeoPoint = new GeoPoint(59879936, 29828610);
    }

    public GeoPoint getLocationGeoPoint() {
	return locationGeoPoint;
    }
    
    public void setLocationGeoPoint(GeoPoint locationGeoPoint) {
	this.locationGeoPoint = locationGeoPoint;
    }

    public int getId() {
	return id;
    }
    
    public void setId(int id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public GeoCacheType getType() {
	return type;
    }

    public void setType(GeoCacheType type) {
	this.type = type;
    }

    public GeoCacheStatus getStatus() {
	return status;
    }

    public void setStatus(GeoCacheStatus status) {
	this.status = status;
    }

}

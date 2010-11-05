package su.geocaching.android.model.dataType;

import com.google.android.maps.GeoPoint;

import java.util.HashMap;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        GeoCache - central concept of geocaching game.
 */
public class GeoCache {
    private GeoPoint locationGeoPoint;    //Coordinates of cache location
    private int id;            //Unique identifier of GeoCache(from geocaching.su)

    //TODO: maybe implement a database of parameters
    private HashMap<String, String> param;

    private void initParam() {
        param = new HashMap<String, String>();

        param.put("name", "name of cache");
        param.put("type", "traditional");
        // may be traditional, virtual, step by step, extreme, event
        param.put("status", "valid");
        // may be valid, not valid, not confirmed that it is not valid
        //TODO: add more parameters of cache
    }

    public HashMap<String, String> getParam() {
        return param;
    }

    public GeoCache() {
        locationGeoPoint = new GeoPoint(0, 0);
        initParam();
    }

    public GeoCache(int id) {
        this.id = id;
        initParam();
        // TODO: retrieve data from API Manager
        locationGeoPoint = new GeoPoint(59879936, 29828610);
    }

    public GeoCache(int latitude, int longitude, int id) {
        this.id = id;
        locationGeoPoint = new GeoPoint(latitude, longitude);
        initParam();
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
        return param.get("name");
    }

    public void setLocationGeoPoint(GeoPoint locationGeoPoint) {
        this.locationGeoPoint = locationGeoPoint;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        param.put("name", name);
    }

    public GeoPoint getLocation() {
        return getLocationGeoPoint();
    }
}

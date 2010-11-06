package su.geocaching.android.model.dataType;

import com.google.android.maps.GeoPoint;

import java.util.HashMap;

/**
 * @author Nikita Bumakov
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

        param.put(NAME, "name of cache");
        param.put(TYPE, TYPE_TRADITIONAL);        
        param.put(STATUS, STATUS_VALID);       
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

    public void setLocationGeoPoint(GeoPoint locationGeoPoint) {
        this.locationGeoPoint = locationGeoPoint;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGeoCachParameter(String parametrId){
	return param.get(parametrId);
    }
    
    public void setGeoCachParameter(String parametrId, String value){
	param.put(parametrId, value);
    }
    
    //parameter types
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String STATUS = "status";
    
    //types of caches
    public static final String TYPE_TRADITIONAL = "traditional";
    public static final String TYPE_VIRTUAL = "virtual";
    public static final String TYPE_STEP_BY_STEP = "step by step";
    public static final String TYPE_EXTREME = "extreme";
    public static final String TYPE_EVENT = "event";
    
    //statuses of caches
    public static final String STATUS_VALID = "valid";
    public static final String STATUS_NOT_VALID = "not valid";
    public static final String STATUS_NOT_CONFIRMED = "not confirmed";
    
    
}

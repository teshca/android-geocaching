package su.geocaching.android.model.datatype;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

/**
 * Class for storing information about the cache
 * 
 * @author Nikita Bumakov
 * @since October 2010 GeoCache - central concept of geocaching game.
 */
public class GeoCache implements Parcelable {

    private int id; // Unique identifier of GeoCache(from geocaching.su)
    private GeoPoint locationGeoPoint;
    private String name;
    private GeoCacheType type;
    private GeoCacheStatus status;

    public GeoCache() {
	locationGeoPoint = new GeoPoint(0, 0);
	type = GeoCacheType.TRADITIONAL;
	status = GeoCacheStatus.VALID;
    }

    @Deprecated
    public GeoCache(int id) {
	this.id = id;
	locationGeoPoint = new GeoPoint(59879936, 29828610);
	type = GeoCacheType.TRADITIONAL;
	status = GeoCacheStatus.VALID;
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

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
	return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
	// ! Important order of writing to parcel
	arg0.writeInt(id);
	arg0.writeString(name);
	int[] data = new int[2];
	data[0] = locationGeoPoint.getLatitudeE6();
	data[1] = locationGeoPoint.getLongitudeE6();
	arg0.writeIntArray(data);
	arg0.writeInt(type.ordinal());
	arg0.writeInt(status.ordinal());
    }

    /**
     * Standard implementation of static final class which need for creating
     * GeoCache object from parcel
     */
    public static final Parcelable.Creator<GeoCache> CREATOR = new Parcelable.Creator<GeoCache>() {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
	 */
	public GeoCache createFromParcel(Parcel in) {
	    // ! Important reading order from parcel
	    GeoCache res = new GeoCache();
	    res.id = in.readInt();
	    res.name = in.readString();
	    int[] location = new int[2];
	    in.readIntArray(location);
	    res.locationGeoPoint = new GeoPoint(location[0], location[1]);
	    res.type = GeoCacheType.values()[in.readInt()];
	    res.status = GeoCacheStatus.values()[in.readInt()];
	    return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable.Creator#newArray(int)
	 */
	public GeoCache[] newArray(int size) {
	    return new GeoCache[size];
	}
    };
}
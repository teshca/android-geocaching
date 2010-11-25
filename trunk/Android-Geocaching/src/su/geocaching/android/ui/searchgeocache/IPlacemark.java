package su.geocaching.android.ui.searchgeocache;

import com.google.android.maps.GeoPoint;

/**
 * Represents a major placemark along a driving route.
 * 
 */
public interface IPlacemark
{
	public abstract GeoPoint getLocation();

	public abstract String getInstructions();

	public abstract String getDistance();

}
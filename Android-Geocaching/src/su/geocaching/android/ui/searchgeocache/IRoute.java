package su.geocaching.android.ui.searchgeocache;

import java.util.List;

import com.google.android.maps.GeoPoint;

/**
 * Represents the driving directions path.
 * The route consists of a ordered list of geographical points and major placemarks.
 * 
 */
public interface IRoute
{
	public abstract String getTotalDistance();

	public abstract List<GeoPoint> getGeoPoints();

	public abstract List<IPlacemark> getPlacemarks();

}

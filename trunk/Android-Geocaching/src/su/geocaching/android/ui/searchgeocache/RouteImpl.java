package su.geocaching.android.ui.searchgeocache;

import java.util.ArrayList;
import java.util.List;


import com.google.android.maps.GeoPoint;

public class RouteImpl implements IRoute

{
	private String totalDistance;
	private List<GeoPoint> geoPoints;
	private List<IPlacemark> placemarks;

	public void setTotalDistance(String totalDistance) {
		this.totalDistance = totalDistance;
	}

	public String getTotalDistance() {
		return totalDistance;
	}
	
	public void setGeoPoints(List<GeoPoint> geoPoints) {
		this.geoPoints = geoPoints;
	}

	public List<GeoPoint> getGeoPoints() {
		return geoPoints;
	}
	
	public void addGeoPoint (GeoPoint point)
	{
		if (geoPoints == null) {
			geoPoints = new ArrayList<GeoPoint>();
		}
		geoPoints.add(point);
	}

	public void setPlacemarks(List<IPlacemark> placemarks) {
		this.placemarks = placemarks;
	}

	public List<IPlacemark> getPlacemarks() {
		return placemarks;
	}
	
	public void addPlacemark (IPlacemark mark)
	{
		if (placemarks == null) {
			placemarks = new ArrayList<IPlacemark>();
		}
		placemarks.add(mark);
	}
}
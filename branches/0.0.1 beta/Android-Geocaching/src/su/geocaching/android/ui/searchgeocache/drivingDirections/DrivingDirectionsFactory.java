package su.geocaching.android.ui.searchgeocache.drivingDirections;

public class DrivingDirectionsFactory
{

	public static DrivingDirections createDrivingDirections() {
		return new DrivingDirectionsGoogleKML() {
		};
		
	}
}
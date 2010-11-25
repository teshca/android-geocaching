package su.geocaching.android.ui.searchgeocache;

public class DrivingDirectionsFactory
{

	public static DrivingDirections createDrivingDirections() {
		return new DrivingDirectionsGoogleKML() {
		};
		
	}
}
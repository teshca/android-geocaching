package su.geocaching.android.model;

public class GeoPoint {

    public GeoPoint(GeoPoint geoPoint) {
        this.latitude = geoPoint.getLatitude();
        this.longitude = geoPoint.getLongitude();
    }

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    private final double latitude;
    private final double longitude;

    @Deprecated
    public static GeoPoint fromE6(int latitude, int longitude) {
        return new GeoPoint(latitude * 1E-6, longitude * 1E-6);
    }

    @Deprecated
    public int getLatitudeE6() {
        return (int) (this.latitude * 1E6);
    }

    @Deprecated
    public int getLongitudeE6() {
        return (int) (this.longitude * 1E6);
    }

    @Override
    public String toString() {
        return String.format("%s, %s", latitude, longitude);
    }
}
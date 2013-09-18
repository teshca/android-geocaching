package su.geocaching.android.ui.map;

import android.location.Location;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import su.geocaching.android.ui.R;

public class GoogleUserLocationLayer {

    protected Marker userMarker;
    protected Polygon userAccuracyPolygon;

    protected GoogleMap googleMap;

    public GoogleUserLocationLayer(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void onUserLocationUpdated(Location location) {
        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            userMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location))
                            .position(userPosition)
            );
        } else {
            userMarker.setPosition(userPosition);
        }

        //TODO: optimize. don't recreate if accuracy is not changed
        if (userAccuracyPolygon != null)
            userAccuracyPolygon.remove();

        userAccuracyPolygon = googleMap.addPolygon(
                getCirclePolygonOption(userPosition, location.getAccuracy())
                        .strokeColor(ACCURACY_CIRCLE_STROKE_COLOR)
                        .strokeWidth(3)
                        .geodesic(true)
                        .fillColor(ACCURACY_CIRCLE_COLOR)
        );
    }

    private static final int ACCURACY_CIRCLE_COLOR = 0x4000aa00;
    private static final int ACCURACY_CIRCLE_STROKE_COLOR = 0x40000000;

    private PolygonOptions getCirclePolygonOption(LatLng center, double radius) {
        PolygonOptions options = new PolygonOptions();

        double d2r = Math.PI / 180;

        double circleLat = (radius / 6378135) / d2r;
        double circleLng = circleLat / Math.cos(center.latitude * d2r);

        for (int i = 0; i <= 360; i++) {
            double theta = i * d2r;
            options.add(new LatLng(center.latitude + circleLat * Math.sin(theta),
                    center.longitude + circleLng * Math.cos(theta)));
        }

        return options;
    }
}

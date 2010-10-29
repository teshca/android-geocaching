package su.geocaching.android.searchGeoCache;

import su.geocaching.android.view.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	Overlay contains of arrow rotated by user azimuth and circle of accuracy
 */
public class UserLocationOverlay extends com.google.android.maps.Overlay {
    private Context context;
    private GeoPoint point;
    private float angle;
    private float radius;

    public UserLocationOverlay(Context context, GeoPoint gp, float angle,
	    float radius) {
	this.context = context;
	this.point = gp;
	this.angle = angle;
	this.radius = radius;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
	    long when) {
	super.draw(canvas, mapView, shadow);

	// Translate the GeoPoint to screen pixels
	Point screenPts = new Point();
	mapView.getProjection().toPixels(point, screenPts);

	// Get default marker
	Bitmap bmpDefault = BitmapFactory.decodeResource(
		context.getResources(), R.drawable.userpoint);

	// Rotate default marker
	Matrix matrix = new Matrix();
	matrix.setRotate(angle);
	Bitmap bmpRotated = Bitmap.createBitmap(bmpDefault, 0, 0,
		bmpDefault.getWidth(), bmpDefault.getHeight(), matrix, true);
	canvas.drawBitmap(bmpRotated, screenPts.x - bmpRotated.getWidth() / 2,
		screenPts.y - bmpRotated.getHeight() / 2, null);
	
	// Draw accuracy circle
	Paint paint = new Paint();
	paint.setColor(0x0000FF);
	canvas.drawCircle(screenPts.x - bmpRotated.getWidth() / 2,
		screenPts.y - bmpRotated.getHeight() / 2, radius, paint);

	return true;
    }

    protected void setPoint(GeoPoint point) {
        this.point = point;
    }

    protected void setAngle(float angle) {
        this.angle = angle;
    }

    protected void setRadius(float radius) {
        this.radius = radius;
    }
}

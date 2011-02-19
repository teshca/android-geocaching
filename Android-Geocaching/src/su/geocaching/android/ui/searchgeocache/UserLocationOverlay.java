package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 20, 2010F
 */
public class UserLocationOverlay extends com.google.android.maps.Overlay {
	private static final int ACCURACY_CIRCLE_ALPHA = 50;
	private static final int ACCURACY_CIRCLE_COLOR = 0xff00aa00;
	private static final int ACCURACY_CIRCLE_STROKE_COLOR = 0xff00ff00;

	private Context context;
	private GeoPoint userPoint;
	private float bearing;
	private float accuracyRadius; // accuracy radius in meters

	/**
	 * @param context activity which use this overlay
	 */
	public UserLocationOverlay(Context context) {
		this.context = context;
		userPoint = null;
		bearing = Float.NaN;
		accuracyRadius = Float.NaN;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean, long)
	 */
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		super.draw(canvas, mapView, shadow);

		if (userPoint == null) {
			return true;
		}

		// Translate the GeoPoint to screen pixels
		Point screenPts = new Point();
		mapView.getProjection().toPixels(userPoint, screenPts);

		// Prepare to draw default marker
		Bitmap userPoint;
		if (Float.isNaN(bearing)) {
			userPoint = BitmapFactory.decodeResource(context.getResources(), R.drawable.userpoint);
		} else {
			userPoint = BitmapFactory.decodeResource(context.getResources(), R.drawable.userarrow);
			// Rotate default marker
			Matrix matrix = new Matrix();
			matrix.setRotate(bearing);
			userPoint = Bitmap.createBitmap(userPoint, 0, 0, userPoint.getWidth(), userPoint.getHeight(), matrix, true);
		}

		// Draw accuracy circle
		if (!Float.isNaN(accuracyRadius)) {
			float radiusInPixels = mapView.getProjection().metersToEquatorPixels(accuracyRadius);
			if ((radiusInPixels > userPoint.getWidth()) && (radiusInPixels > userPoint.getHeight())) {
				Paint paintCircle = new Paint();
				paintCircle.setColor(ACCURACY_CIRCLE_COLOR);
				paintCircle.setAntiAlias(true);
				paintCircle.setAlpha(ACCURACY_CIRCLE_ALPHA);

				Paint paintStroke = new Paint();
				paintStroke.setColor(ACCURACY_CIRCLE_STROKE_COLOR);
				paintStroke.setAntiAlias(true);
				paintStroke.setStyle(Style.STROKE);
				paintStroke.setAlpha(ACCURACY_CIRCLE_ALPHA);

				canvas.drawCircle(screenPts.x, screenPts.y, radiusInPixels, paintCircle);
				canvas.drawCircle(screenPts.x, screenPts.y, radiusInPixels, paintStroke);
			}
		}

		// Draw default marker
		canvas.drawBitmap(userPoint, screenPts.x - userPoint.getWidth() / 2, screenPts.y - userPoint.getHeight() / 2, null);
		return true;
	}

	/**
	 * @param point set user location
	 */
	public void setPoint(GeoPoint point) {
		this.userPoint = point;
	}

	/**
	 * @param angle set user bearing on North
	 */
	public void setBearing(float angle) {
		this.bearing = angle;
	}

	/**
	 * @param radius set accuracy radius of location point
	 */
	public void setAccuracy(float radius) {
		this.accuracyRadius = radius;
	}
}

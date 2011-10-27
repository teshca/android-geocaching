package su.geocaching.android.ui.searchmap;

import android.graphics.*;
import android.graphics.Paint.Style;
import android.location.Location;
import com.google.android.maps.MapView;

import su.geocaching.android.controller.compass.ICompassAnimation;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.UserLocationOverlayBase;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since November 2010
 */
public class DynamicUserLocationOverlay extends UserLocationOverlayBase implements ICompassAnimation {
    private static final double COMPASS_ARROW_WIDTH_COEFF = 23.0 / 320.0;
    private static final double COMPASS_ARROW_HEIGHT_COEFF = 32.0 / 480.0;
    private static final int MAP_INVALIDATE_INTERVAL = 50; // milliseconds

    private float bearing;
    private Paint paintCompassArrow;
    private Paint paintStrokeCompassArrow;
    private Path pathCompassArrow;
    private MapView map;
    private long lastTimeInvalidate;
    private int compassArrowWidth;
    private int compassArrowHeight;

    public DynamicUserLocationOverlay(SearchMapActivity context, MapView map) {
        bearing = Float.NaN;

        this.map = map;
        lastTimeInvalidate = -1;

        paintCompassArrow = new Paint();
        paintCompassArrow.setAntiAlias(true);
        paintCompassArrow.setStyle(Style.FILL);
        paintCompassArrow.setColor(map.getResources().getColor(R.color.user_location_arrow_color_precise));

        paintStrokeCompassArrow = new Paint();
        paintStrokeCompassArrow.setColor(map.getResources().getColor(R.color.user_location_arrow_stroke_color));
        paintStrokeCompassArrow.setStyle(Style.STROKE);
        paintStrokeCompassArrow.setStrokeWidth(2);
        paintStrokeCompassArrow.setAntiAlias(true);

        pathCompassArrow = new Path();
        compassArrowWidth = (int) (Math.min(context.getWindowManager().getDefaultDisplay().getWidth(), context.getWindowManager().getDefaultDisplay().getHeight()) * COMPASS_ARROW_WIDTH_COEFF);
        compassArrowHeight = (int) (Math.max(context.getWindowManager().getDefaultDisplay().getWidth(), context.getWindowManager().getDefaultDisplay().getHeight()) * COMPASS_ARROW_HEIGHT_COEFF);
        int h = compassArrowHeight / 2;
        int w = compassArrowWidth / 2;
        pathCompassArrow.moveTo(0, h / 2);
        pathCompassArrow.lineTo(-w, h);
        pathCompassArrow.lineTo(0, -h);
        pathCompassArrow.lineTo(w, h);
        pathCompassArrow.lineTo(0, h / 2);
        pathCompassArrow.close();
    }

    @Override
    protected void drawUserLocation(Canvas canvas) {
        canvas.save();
        canvas.translate(userPoint.x, userPoint.y);
        canvas.rotate(bearing);
        canvas.drawPath(pathCompassArrow, paintCompassArrow);
        canvas.drawPath(pathCompassArrow, paintStrokeCompassArrow);
        canvas.restore();
    }

    public void updateLocation(Location location) {
        super.updateLocation(location);
        postInvalidate();
    }

    public Rect getBounds()
    {
        final int max = Math.max(Math.max(compassArrowHeight, compassArrowWidth), accuracyRadiusInPixels.intValue());
        return new Rect(-max, -max, 2 * max, 2 * max);
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.compass.ICompassAnimation#setDirection(float)
     */
    @Override
    public boolean setDirection(float direction) {
        bearing = direction;
        postInvalidate();
        return true;
    }

    private void postInvalidate() {
        if (!locationAvailable || System.currentTimeMillis() - lastTimeInvalidate < MAP_INVALIDATE_INTERVAL) {
            return;
        }
        int max = Math.max(compassArrowHeight, compassArrowWidth);
        map.postInvalidate(userPoint.x - max, userPoint.y - max, userPoint.x + max, userPoint.y + max);
        lastTimeInvalidate = System.currentTimeMillis();
    }

    @Override
    protected void onTapAction() {
        NavigationManager.startCompassActivity(map.getContext());
    }

    /**
     * Change behaviour of arrow if location precise or not
     *
     * @param isLocationPrecise true if user location precise
     * @see su.geocaching.android.controller.managers.UserLocationManager#hasPreciseLocation()
     */
    public void setLocationPrecise(boolean isLocationPrecise) {
        if (isLocationPrecise) {
            paintCompassArrow.setColor(map.getResources().getColor(R.color.user_location_arrow_color_precise));
        } else {
            paintCompassArrow.setColor(map.getResources().getColor(R.color.user_location_arrow_color_not_precise));
        }
    }
}

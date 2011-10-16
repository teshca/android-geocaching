package su.geocaching.android.controller.compass;

import android.graphics.*;
import android.graphics.Paint.Style;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.ui.R;

/**
 * Default appearance of the compass
 * 
 * @author Nikita Bumakov
 */
public class DefaultCompassDrawing extends AbstractCompassDrawing {

    protected Paint bitmapPaint = new Paint();
    protected Paint textPaint = new Paint();
    protected Bitmap roseBitmap, needleBitmap, arrowBitmap;

    public DefaultCompassDrawing() {
        super();

        roseBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.compass_rose_yellow);

        textPaint.setColor(Color.parseColor(Controller.getInstance().getResourceManager().getString(R.color.dashboard_text_color)));
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Style.STROKE);
        textPaint.setStrokeWidth(0.8f);
        
        bitmapPaint.setFilterBitmap(true);
    }

    private int bitmapX, bitmapY, size;

    @Override
    public void onSizeChanged(int w, int h) {
        size = Math.min(h, w);
        centerX = w / 2;
        centerY = h / 2;
        needleWidth = size / 30;
        textPaint.setStrokeWidth((float) size / 300);
        roseBitmap = Bitmap.createScaledBitmap(roseBitmap, size, size, true);
        bitmapX = -roseBitmap.getWidth() / 2;
        bitmapY = -roseBitmap.getHeight() / 2;
        needleBitmap = createNeedle();
        arrowBitmap = createCacheArrow();
        textPaint.setTextSize(size * 0.1f);
    }

    @Override
    public void draw(Canvas canvas, float northDirection) {
        canvas.drawColor(bgColor);
        canvas.translate(centerX, centerY); // !!!
        canvas.drawBitmap(roseBitmap, bitmapX, bitmapY, bitmapPaint);

        drawNeedle(canvas, northDirection);
        drawAzimuthLabel(canvas, northDirection);
        drawDistanceLabel(canvas);
    }

    private void drawNeedle(Canvas canvas, float direction) {
        canvas.rotate(direction);
        canvas.drawBitmap(needleBitmap, -needleBitmap.getWidth() / 2, -needleBitmap.getHeight() / 2, bitmapPaint);
        canvas.rotate(-direction);
    }

    private void drawAzimuthLabel(Canvas canvas, float direction) {
        canvas.drawText(CompassHelper.degreesToString(direction, AZIMUTH_FORMAT), centerX * 0.4f, -centerY * 0.8f, textPaint);
    }

    private void drawDistanceLabel(Canvas canvas) {
        boolean hasPreciseLocation = Controller.getInstance().getLocationManager().hasPreciseLocation();
        canvas.drawText(CoordinateHelper.distanceToString(distance, hasPreciseLocation), -centerX * 0.95f, -centerY * 0.8f, textPaint);
    }

    @Override
    public void drawCacheArrow(Canvas canvas, float direction) {
        canvas.rotate(direction);
        canvas.drawBitmap(arrowBitmap, -arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() / 2, bitmapPaint);
        canvas.rotate(-direction);
    }

    private Bitmap createNeedle() {
        Bitmap bitmap = Bitmap.createBitmap(needleWidth * 3, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path needlePath = new Path();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);

        float top = size / 2 * 0.85f;
        canvas.translate(needleWidth * 1.5f, size / 2);
        needlePath.moveTo(-needleWidth, 0);
        needlePath.lineTo(0, -top);
        needlePath.lineTo(needleWidth, 0);
        needlePath.close();

        paint.setARGB(200, 0, 0, 255);
        canvas.drawPath(needlePath, paint);

        canvas.rotate(180);
        paint.setARGB(200, 255, 0, 0);
        canvas.drawPath(needlePath, paint);

        paint.setColor(Color.argb(255, 255, 230, 110));
        canvas.drawCircle(0, 0, needleWidth * 1.5f, paint);

        return bitmap;
    }

    private Bitmap createCacheArrow() {
        Bitmap bitmap = Bitmap.createBitmap(needleWidth * 3, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path arrowPath = new Path();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);

        float top = size / 2 * 0.85f;
        canvas.translate(needleWidth * 1.5f, size / 2);
        arrowPath.moveTo(-needleWidth, 0);
        arrowPath.lineTo(0, -top);
        arrowPath.lineTo(needleWidth, 0);
        arrowPath.close();

        paint.setARGB(200, 60, 200, 90);
        canvas.drawPath(arrowPath, paint);

        paint.setColor(Color.argb(255, 255, 230, 110));
        canvas.drawCircle(0, 0, needleWidth * 1.5f, paint);

        return bitmap;
    }
}

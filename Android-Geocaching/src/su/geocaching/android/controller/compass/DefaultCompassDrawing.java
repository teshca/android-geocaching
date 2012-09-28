package su.geocaching.android.controller.compass;

import android.graphics.*;
import android.graphics.Paint.Align;
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
    protected Paint distanceTextPaint = new Paint();
    protected Paint azimuthTextPaint = new Paint();
    protected Paint declinationTextPaint = new Paint();
    protected Paint needlePaint = new Paint();
    protected Bitmap roseBitmap, needleBitmap, greenArrowBitmap, grayArrowBitmap,gpsSourceBitmap;
    private Bitmap scaledBitmap;

    public DefaultCompassDrawing() {
        super();

        roseBitmap = createRouse();
        gpsSourceBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.ic_satellite_dish);

        int dashboardColor = Color.parseColor(Controller.getInstance().getResourceManager().getString(R.color.dashboard_text_color)); 
        
        distanceTextPaint.setColor(dashboardColor);
        distanceTextPaint.setAntiAlias(true);
        distanceTextPaint.setTextAlign(Align.LEFT);
        distanceTextPaint.setStyle(Style.STROKE);
        distanceTextPaint.setStrokeWidth(0.8f);
        
        azimuthTextPaint.setColor(dashboardColor);
        azimuthTextPaint.setAntiAlias(true);
        azimuthTextPaint.setTextAlign(Align.RIGHT);
        azimuthTextPaint.setStyle(Style.STROKE);
        azimuthTextPaint.setStrokeWidth(0.8f);        
        
        declinationTextPaint.setColor(dashboardColor);
        declinationTextPaint.setAntiAlias(true);
        declinationTextPaint.setTextAlign(Align.RIGHT);
        declinationTextPaint.setStyle(Style.STROKE);
        declinationTextPaint.setStrokeWidth(0.8f);

        needlePaint.setAntiAlias(true);
        needlePaint.setStyle(Style.FILL_AND_STROKE);
        needlePaint.setStrokeWidth(1);

        bitmapPaint.setFilterBitmap(true);
    }

    protected Bitmap createRouse() {
        return BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.compass_rose_yellow);
    }

    private int bitmapX, bitmapY, size;

    @Override
    public void onSizeChanged(int w, int h) {
        int newSize = Math.min(h, w);

        centerX = w / 2;
        centerY = h / 2;

        if (newSize == size) return;

        synchronized(bitmapPaint) {
            size = newSize;
            needleWidth = size / 30;

            recycleBitmaps();

            scaledBitmap = Bitmap.createScaledBitmap(roseBitmap, size, size, true);
            bitmapX = -scaledBitmap.getWidth() / 2;
            bitmapY = -scaledBitmap.getHeight() / 2;
            needleBitmap = createNeedle();
            greenArrowBitmap = createGreenCacheArrow();
            grayArrowBitmap = createGrayCacheArrow();
            LogManager.d("COMPASS", "CREATED");
            distanceTextPaint.setTextSize(size * 0.1f);
            distanceTextPaint.setStrokeWidth((float) size / 300);
            azimuthTextPaint.setTextSize(size * 0.1f);
            azimuthTextPaint.setStrokeWidth((float) size / 300);
            declinationTextPaint.setTextSize(size * 0.06f);
            declinationTextPaint.setStrokeWidth((float) size / 600);
        }
    }

    private void recycleBitmaps() {
        if (scaledBitmap != null) scaledBitmap.recycle();
        if (needleBitmap != null) needleBitmap.recycle();
        if (greenArrowBitmap != null) greenArrowBitmap.recycle();
        if (grayArrowBitmap != null) grayArrowBitmap.recycle();
    }

    @Override
    public void draw(Canvas canvas, float northDirection) {
        synchronized(bitmapPaint) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.translate(centerX, centerY); // !!!
            canvas.drawBitmap(scaledBitmap, bitmapX, bitmapY, bitmapPaint);

            drawNeedle(canvas, northDirection);
            drawAzimuthLabel(canvas, northDirection);
            drawDistanceLabel(canvas);
            drawDeclinationLabel(canvas);
        }
    }

    private void drawNeedle(Canvas canvas, float direction) {
        synchronized(bitmapPaint) {
            canvas.rotate(direction);
            canvas.drawBitmap(needleBitmap, -needleBitmap.getWidth() / 2, -needleBitmap.getHeight() / 2, bitmapPaint);
            canvas.rotate(-direction);
        }
    }

    @Override
    public void drawSourceType(Canvas canvas, CompassSourceType sourceType) {
        if (sourceType == CompassSourceType.GPS) {
            int halfWidth = gpsSourceBitmap.getWidth() / 2;
            int halfHeight = gpsSourceBitmap.getHeight() / 2;
            canvas.drawBitmap(gpsSourceBitmap, -halfWidth,  -halfHeight, bitmapPaint);
        }
    }

    private void drawAzimuthLabel(Canvas canvas, float direction) {
        int x = Math.round(centerX * 0.95f);
        int y = -Math.round(centerY * 0.8f);
        canvas.drawText(CompassHelper.degreesToString(direction, AZIMUTH_FORMAT), x, y, azimuthTextPaint);
    }

    private void drawDeclinationLabel(Canvas canvas) {
        int x = Math.round(centerX * 0.95f);
        int y = -Math.round(centerY * 0.68f);
        canvas.drawText(String.format(AZIMUTH_FORMAT, declination), x, y, declinationTextPaint);
    }   
    
    private void drawDistanceLabel(Canvas canvas) {
        boolean hasPreciseLocation = Controller.getInstance().getLocationManager().hasPreciseLocation();
        int x = -Math.round(centerX * 0.95f);
        int y = -Math.round(centerY * 0.8f);
        canvas.drawText(CoordinateHelper.distanceToString(distance, hasPreciseLocation), x, y, distanceTextPaint);
    }

    @Override
    public void drawCacheArrow(Canvas canvas, float direction) {
        synchronized(bitmapPaint) {
            canvas.rotate(direction);
            Bitmap arrowBitmap = Controller.getInstance().getLocationManager().hasPreciseLocation() ? greenArrowBitmap : grayArrowBitmap;
            canvas.drawBitmap(arrowBitmap, -arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() / 2, bitmapPaint);
            canvas.rotate(-direction);
        }
    }

    private Path createNeedlePath() {
        Path needlePath = new Path();
        needlePath.moveTo(-needleWidth, 0);
        needlePath.lineTo(0, -size * 0.425f);
        needlePath.lineTo(needleWidth, 0);
        needlePath.close();
        return needlePath;
    }

    private Bitmap createNeedle() {
        Bitmap bitmap = Bitmap.createBitmap(needleWidth * 3, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path needlePath = createNeedlePath();

        canvas.translate(needleWidth * 1.5f, size / 2);

        needlePaint.setARGB(200, 255, 0, 0);
        canvas.drawPath(needlePath, needlePaint);

        canvas.rotate(180);
        needlePaint.setARGB(200, 0, 0, 255);
        canvas.drawPath(needlePath, needlePaint);

        needlePaint.setColor(Color.argb(255, 255, 230, 110));
        canvas.drawCircle(0, 0, needleWidth * 1.5f, needlePaint);

        return bitmap;
    }

    private Bitmap createCacheArrow(int color) {
        Bitmap bitmap = Bitmap.createBitmap(needleWidth * 3, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path arrowPath = createNeedlePath();

        canvas.translate(needleWidth * 1.5f, size / 2);

        needlePaint.setColor(color);
        canvas.drawPath(arrowPath, needlePaint);

        needlePaint.setColor(Color.argb(255, 255, 230, 110));
        canvas.drawCircle(0, 0, needleWidth * 1.5f, needlePaint);

        return bitmap;
    }

    private Bitmap createGreenCacheArrow() {
        return createCacheArrow(Color.argb(200, 60, 200, 90));
    }

    private Bitmap createGrayCacheArrow() {
        return createCacheArrow(Color.argb(200, 84, 84, 84));
    }

    public void destroy() {
        recycleBitmaps();
        roseBitmap.recycle();
        gpsSourceBitmap.recycle();
    }

    @Override
    public String getType() {
        return TYPE_CLASSIC;
    }
}

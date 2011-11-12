package su.geocaching.android.controller.compass;

import android.graphics.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
public class PreviewCompassDrawing extends AbstractCompassDrawing {

    private float direction;
    private Paint bitmapPaint = new Paint();

    private static Bitmap arrowBitmap;       //one bitmap used for all views     TODO recycle it

    public PreviewCompassDrawing() {
        super();
        bitmapPaint.setFilterBitmap(true);
        if (arrowBitmap == null) {
            arrowBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.ic_arrow);
        }
    }

    @Override
    public void draw(Canvas canvas, float northDirection) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.save();
        canvas.translate(centerX, centerY); // !!!
        canvas.rotate(direction);
        canvas.drawBitmap(arrowBitmap, -arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() / 2, bitmapPaint);
        canvas.rotate(-direction);
        canvas.restore();
    }

    @Override
    public void onSizeChanged(int w, int h) {
        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    public void drawCacheArrow(Canvas canvas, float direction) {
        this.direction = direction;
    }

    @Override
    public String getType() {
        return TYPE_PREVIEW;
    }
}

package su.geocaching.android.ui.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoPoint;

public class ScaleView extends View {
    private Paint linePaint;
    private Paint textPaint;

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        linePaint = new Paint();
        linePaint.setAntiAlias(false);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.DKGRAY);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(2);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(16);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setColor(Color.DKGRAY);
    }

    @Override
    protected void onDraw(Canvas c) {
        if (mpp == 0)
            return;

        int w = getWidth();
        int m = (int) (mpp * w / 8);
        if (m < 10)
            m = m;
        else if (m < 40)
            m = m / 10 * 10;
        else if (m < 80)
            m = 50;
        else if (m < 130)
            m = 100;
        else if (m < 300)
            m = 200;
        else if (m < 700)
            m = 500;
        else if (m < 900)
            m = 800;
        else if (m < 1300)
            m = 1000;
        else if (m < 3000)
            m = 2000;
        else if (m < 7000)
            m = 5000;
        else if (m < 10000)
            m = 8000;
        else if (m < 80000)
            m = (int) (Math.ceil(m * 1. / 10000) * 10000);
        else
            m = (int) (Math.ceil(m * 1. / 100000) * 100000);

        int x = (int) (m / mpp);

        if (x > w / 4)
        {
            x /= 2;
            m /= 2;
        }

        final int x2 = x * 2;
        final int x3 = x * 3;
        final int xd2 = x / 2;
        final int xd4 = x / 4;

        int cx = 140;
        int cy = 30;

        c.drawLine(cx, cy, cx+x3, cy, linePaint);
        c.drawLine(cx, cy+10, cx+x3, cy+10, linePaint);
        c.drawLine(cx, cy, cx, cy+10, linePaint);
        c.drawLine(cx+x3, cy, cx+x3, cy+10, linePaint);
        c.drawLine(cx+x, cy, cx+x, cy+10, linePaint);
        c.drawLine(cx+x2, cy, cx+x2, cy+10, linePaint);
        c.drawLine(cx+x, cy+5, cx+x2, cy+5, linePaint);
        c.drawLine(cx, cy+5, cx+xd4, cy+5, linePaint);
        c.drawLine(cx+xd2, cy+5, cx+xd2+xd4, cy+5, linePaint);
        c.drawLine(cx+xd4, cy, cx+xd4, cy+10, linePaint);
        c.drawLine(cx+xd2, cy, cx+xd2, cy+10, linePaint);
        c.drawLine(cx+xd2+xd4, cy, cx+xd2+xd4, cy+10, linePaint);

        int cty = -10;

        c.drawText("0", cx+x, cy+cty, textPaint);
        int threshold = 2000;
        if (m <= threshold && m * 2 > threshold)
            threshold = m * 3;

        String[] d = CoordinateHelper.distanceC(m, threshold);
        c.drawText(d[0], cx+x2, cy+cty, textPaint);
        c.drawText(d[0], cx, cy+cty, textPaint);
        c.drawText(CoordinateHelper.distanceH(m*2, threshold), cx+x3, cy+cty, textPaint);
    }

    public void updateMapViewPort(GeoRect viewPort) {
        int width = getWidth();
        if (width == 0) return;
        GeoPoint bottomLeft = new GeoPoint(viewPort.br.getLatitude(), viewPort.tl.getLongitude());
        double newScale = CoordinateHelper.getDistanceBetween(viewPort.br, bottomLeft) / width;
        if (Math.abs(mpp - newScale) < 0.0001) {
            mpp = newScale;
            invalidate();
        }
    }

    private double mpp;
}

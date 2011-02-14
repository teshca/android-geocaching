package su.geocaching.android.controller.compass;

import su.geocaching.android.ui.R;
import su.geocaching.android.utils.CompassHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

/**
 * Default appearance of the compass
 * 
 * @author Nikita Bumakov
 */
public class StandartCompassDrawning extends CompassDrawningHelper {

	private Paint bitmapPaint = new Paint();
	private Paint textPaint = new Paint();
	private Bitmap roseBitmap, needleBitmap, arrowBitmap;

	public StandartCompassDrawning(Context context) {
		super(context);

		roseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass);

		textPaint.setColor(Color.parseColor(context.getString(R.color.menu_text_color)));
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Style.STROKE);
		textPaint.setStrokeWidth(0.8f);
	}

	public void onSizeChanged(int w, int h) {
		int size = Math.min(h, w);
		center = size / 2;
		roseBitmap = Bitmap.createScaledBitmap(roseBitmap, size, size, true);
		needleBitmap = createNeedle();
		arrowBitmap = createCacheArrow();
		textPaint.setTextSize(center * 0.2f);

	}

	public void draw(Canvas canvas, float northDirection) {
		canvas.drawColor(bgColor);
		canvas.drawBitmap(roseBitmap, 0, 0, bitmapPaint);
		canvas.translate(center, center); // !!!
		drawNeedle(canvas, northDirection);
		drawDirectionLabel(canvas, northDirection);
	}

	private void drawDirectionLabel(Canvas canvas, float direction) {
		canvas.drawText(CompassHelper.degreesToString(direction), 5 - center, -center * 0.8f, textPaint);
	}

	private void drawNeedle(Canvas canvas, float direction) {
		canvas.rotate(direction);
		canvas.drawBitmap(needleBitmap, -needleBitmap.getWidth() / 2, -needleBitmap.getHeight() / 2, bitmapPaint);
		canvas.rotate(-direction);
	}

	public void drawCacheArrow(Canvas canvas, float direction) {
		canvas.rotate(direction);
		canvas.drawBitmap(arrowBitmap, -arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() / 2, bitmapPaint);
		canvas.rotate(-direction);
	}

	private Bitmap createNeedle() {
		Bitmap bitmap = Bitmap.createBitmap(needleWidth * 3, center * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Path needlePath = new Path();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(1);

		float top = center * 0.85f;
		canvas.translate(needleWidth * 1.5f, center);
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
		Bitmap bitmap = Bitmap.createBitmap(20, center * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Path arrowPath = new Path();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(1);

		float top = center * 0.85f;
		canvas.translate(10, center);
		arrowPath.moveTo(-10, 0);
		arrowPath.lineTo(0, -top);
		arrowPath.lineTo(10, 0);
		arrowPath.close();

		paint.setARGB(200, 60, 200, 90);
		canvas.drawPath(arrowPath, paint);

		paint.setColor(Color.argb(255, 255, 230, 110));
		canvas.drawCircle(0, 0, needleWidth * 1.5f, paint);

		return bitmap;
	}
}

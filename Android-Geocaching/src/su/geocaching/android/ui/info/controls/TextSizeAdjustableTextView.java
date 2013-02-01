package su.geocaching.android.ui.info.controls;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class TextSizeAdjustableTextView extends TextView {

    public TextSizeAdjustableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextSizeAdjustableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextSizeAdjustableTextView(Context context) {
        super(context);
    }

    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        TextPaint textPaint = getPaint();
        float size = textPaint.getTextSize();
        int textViewWidth = getMeasuredWidth();
        float stringWidth = textPaint.measureText(getText().toString());
        float percent = (textViewWidth / stringWidth);
        if (percent < 0.99) {
            float newTextSize = size*percent;
            textPaint.setTextSize(newTextSize);
            onTextSizeAdjusted(newTextSize);
        }
    }

    private void onTextSizeAdjusted(float newTextSize) {
        fireTextSizeAdjusted(new TextSizeAdjustedEvent(newTextSize));
    }

    private Set<TextSizeAdjustedListener> textSizeAdjustedEventListeners = new HashSet<TextSizeAdjustedListener>();

    public synchronized void addTextSizeAdjustedListener(TextSizeAdjustedListener listener)
    {
        textSizeAdjustedEventListeners.add(listener);
    }
    public synchronized void removeTextSizeAdjustedListener(TextSizeAdjustedListener listener)
    {
        textSizeAdjustedEventListeners.remove(listener);
    }

    private synchronized void fireTextSizeAdjusted(TextSizeAdjustedEvent textSizeAdjustedListenerEvent)
    {
        for (TextSizeAdjustedListener textSizeAdjustedEventListener: textSizeAdjustedEventListeners)
        {
            textSizeAdjustedEventListener.textSizeAdjusted(textSizeAdjustedListenerEvent);
        }
    }
}

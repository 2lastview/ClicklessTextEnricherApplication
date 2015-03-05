package com.example.moritztomasi.clicklesstextenricherapplication.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

public class RuledEditText extends TextView {

    private Rect rect;
    private Paint paint;

    public RuledEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        rect = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0x7EA6E0FF);
        paint.setStrokeWidth(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        Rect r = rect;
        Paint paint = this.paint;

        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
        }

        super.onDraw(canvas);
    }
}

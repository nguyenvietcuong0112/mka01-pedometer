package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CountdownView extends View {

    private Paint paint;
    private RectF oval;
    private float sweepAngle = 360;

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(0xFFFF5722);
        oval = new RectF();
    }

    public void setSweepAngle(float angle) {
        this.sweepAngle = angle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = paint.getStrokeWidth() / 2;
        oval.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawArc(oval, 270, -sweepAngle, false, paint); // quay ngược
    }
}
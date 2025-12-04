package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomCircularProgressBar extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float progress = 0f; // 0-100
    private int strokeWidth = 20;

    public CustomCircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFDDE9FB); // #DDE9FB
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(0xFF1E6BE6); // #1E6BE6
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - strokeWidth;

        rectF.set(
                width / 2f - radius,
                height / 2f - radius,
                width / 2f + radius,
                height / 2f + radius
        );

        // Vẽ background circle
        canvas.drawCircle(width / 2f, height / 2f, radius, backgroundPaint);

        // Vẽ progress arc (bắt đầu từ -90° = 12 giờ)
        float sweepAngle = 360f * (progress / 100f);
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }
}
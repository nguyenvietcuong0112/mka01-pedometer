package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class ArcProgressView extends View {

    private Paint bgPaint;
    private Paint fgPaint;
    private RectF arcRect = new RectF();

    private float strokeDp = 14f;
    private float strokePx;

    private float startAngle = 180f;
    private float sweepAngle = 180f;

    private float goalWeight = 100f;
    private float currentWeight = 0f;

    private View badgeView;

    private int bgColor = 0xFFE6E9F5;
    private int fgColorStart = 0xFF768CFE;
    private int fgColorEnd = 0xFF4C6BFF;

    private float cx, cy, radius;

    private float sidePaddingDp = 16f;
    private float sidePaddingPx = 0f;

    private float badgeOffsetDp = 4f;
    private float badgeOffsetPx = 0f;

    public ArcProgressView(Context context) {
        super(context);
        init();
    }

    public ArcProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokePx = dpToPx(strokeDp);
        sidePaddingPx = dpToPx(sidePaddingDp);
        badgeOffsetPx = dpToPx(badgeOffsetDp);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(strokePx);
        bgPaint.setColor(bgColor);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeWidth(strokePx);
        fgPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setBadgeView(View badge) {
        this.badgeView = badge;
        invalidate();
    }

    public void setBadgeOffsetDp(float dp) {
        this.badgeOffsetDp = dp;
        this.badgeOffsetPx = dpToPx(dp);
        invalidate();
    }

    public void setSidePaddingDp(float dp) {
        this.sidePaddingDp = dp;
        this.sidePaddingPx = dpToPx(dp);
        requestLayout();
    }

    public void setGoalWeight(float g) {
        if (Float.isNaN(g) || g <= 0f) this.goalWeight = 100f;
        else this.goalWeight = g;
        invalidate();
    }

    public void setCurrentWeight(float c) {
        if (Float.isNaN(c) || c < 0f) c = 0f;
        this.currentWeight = c;
        invalidate();
    }

    public float getCurrentWeight() {
        return this.currentWeight;
    }

    public void animateCurrentWeight(float from, float to, long durationMs) {
        if (Float.isNaN(from)) from = 0f;
        if (Float.isNaN(to)) to = 0f;
        ValueAnimator a = ValueAnimator.ofFloat(from, to);
        a.setDuration(Math.max(0, (int) durationMs));
        a.addUpdateListener(animation -> {
            float v = (float) animation.getAnimatedValue();
            setCurrentWeight(v);
        });
        a.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float strokePad = strokePx / 2f + dpToPx(6f);

        float availW = w - 2f * strokePad - 2f * sidePaddingPx;
        float availH = h - 2f * strokePad;

        float r = Math.min(availW / 2f, availH);

        float leftBound = sidePaddingPx + strokePad;
        float rightBound = w - sidePaddingPx - strokePad;
        float centerX = (leftBound + rightBound) / 2f;

        float centerY = strokePad + r;

        this.cx = centerX;
        this.cy = centerY;
        this.radius = Math.max(0f, r);

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        Shader sweep = new SweepGradient(cx, cy, new int[]{fgColorStart, fgColorEnd}, null);
        Matrix m = new Matrix();
        m.preRotate(startAngle - 90f, cx, cy);
        sweep.setLocalMatrix(m);
        fgPaint.setShader(sweep);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(arcRect, startAngle, sweepAngle, false, bgPaint);

        float ratio = 0f;
        if (goalWeight > 0f) ratio = currentWeight / goalWeight;
        ratio = Math.max(0f, Math.min(1f, ratio));

        canvas.drawArc(arcRect, startAngle, sweepAngle * ratio, false, fgPaint);

        positionBadge(ratio);
    }

    private void positionBadge(float ratio) {
        if (badgeView == null) return;

        float angleDeg = startAngle + sweepAngle * ratio;
        double angleRad = Math.toRadians(angleDeg);

        float strokeHalf = strokePx / 2f;

        float distance = radius + strokeHalf - badgeOffsetPx;

        float minDist = Math.max(radius * 0.3f, strokeHalf + dpToPx(2f));
        if (distance < minDist) distance = minDist;

        float markerX = (float) (cx + distance * Math.cos(angleRad));
        float markerY = (float) (cy + distance * Math.sin(angleRad));

        if (badgeView.getMeasuredWidth() == 0 || badgeView.getMeasuredHeight() == 0) {
            badgeView.post(() -> {
                int hw = badgeView.getMeasuredWidth() / 2;
                int hh = badgeView.getMeasuredHeight() / 2;
                badgeView.setX(markerX - hw);
                badgeView.setY(markerY - hh);
            });
        } else {
            int hw = badgeView.getMeasuredWidth() / 2;
            int hh = badgeView.getMeasuredHeight() / 2;
            badgeView.setX(markerX - hw);
            badgeView.setY(markerY - hh);
        }
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

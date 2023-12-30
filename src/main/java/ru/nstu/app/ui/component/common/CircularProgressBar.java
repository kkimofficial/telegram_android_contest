package ru.nstu.app.ui.component.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.DisplayController;

public class CircularProgressBar extends View {
    public static int DIAMETER_BLACK = 48;
    public static int DIAMETER_BLUE = 40;

    private RadialProgress radialProgress;

    public CircularProgressBar(Context context) {
        super(context);

        radialProgress = new RadialProgress(this);

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        radialProgress.onDraw(canvas);
    }

    public void setSize(int diameter) {
        radialProgress.setProgressRect(0, 0, DisplayController.dp(diameter), DisplayController.dp(diameter));
    }

    public void setProgress(float value) {
        radialProgress.setProgress(value, true);
    }

    public void reset() {
        radialProgress.setProgress(0.0f, false);
    }

    public void finish(Drawable content) {
        radialProgress.setBackground(content, false, true);
    }

    public void setProgressColor(int color) {
        radialProgress.setProgressColor(color);
    }

    public void setBackgroundColor(int color) {
        radialProgress.setBackgroundColor(color);
    }

    public void setContent(Drawable content, boolean withRound, boolean animated) {
        radialProgress.setBackground(content, withRound, animated);
    }

    // ==============================================================
    /*
     * This is the source code of Telegram for Android v. 2.0.x.
     * It is licensed under GNU GPL v. 2 or later.
     * You should have received a copy of the license in this archive (see LICENSE).
     *
     * Copyright Nikolai Kudashov, 2013-2014.
     */
    public static class RadialProgress {

        private long lastUpdateTime = 0;
        private float radOffset = 0;
        private float currentProgress = 0;
        private float animationProgressStart = 0;
        private long currentProgressTime = 0;
        private float animatedProgressValue = 0;
        private RectF progressRect = new RectF();
        private RectF cicleRect = new RectF();
        private View parent = null;
        private float animatedAlphaValue = 1.0f;

        private boolean currentWithRound;
        private boolean previousWithRound;
        private Drawable currentDrawable;
        private Drawable previousDrawable;
        private boolean hideCurrentDrawable;

        private DecelerateInterpolator decelerateInterpolator = null;
        private Paint progressPaint = null;

        private Paint backgroundPaint = null;
        private int backgroundColor;

        public RadialProgress(View parentView) {
            if (decelerateInterpolator == null) {
                decelerateInterpolator = new DecelerateInterpolator();
                progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                progressPaint.setStyle(Paint.Style.STROKE);
                progressPaint.setStrokeCap(Paint.Cap.ROUND);
                progressPaint.setStrokeWidth(DisplayController.dp(2));
                progressPaint.setColor(0xffffffff);

                backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                backgroundPaint.setStyle(Paint.Style.FILL);
                backgroundPaint.setColor(backgroundColor = 0xff000000);
            }
            parent = parentView;
        }

        public void setProgressRect(int left, int top, int right, int bottom) {
            progressRect.set(left, top, right, bottom);
        }

        private void updateAnimation() {
            long newTime = System.currentTimeMillis();
            long dt = newTime - lastUpdateTime;
            lastUpdateTime = newTime;

            if (animatedProgressValue != 1) {
                radOffset += 360 * dt / 3000.0f;
                float progressDiff = currentProgress - animationProgressStart;
                if (progressDiff > 0) {
                    currentProgressTime += dt;
                    if (currentProgressTime >= 300) {
                        animatedProgressValue = currentProgress;
                        animationProgressStart = currentProgress;
                        currentProgressTime = 0;
                    } else {
                        animatedProgressValue = animationProgressStart + progressDiff * decelerateInterpolator.getInterpolation(currentProgressTime / 300.0f);
                    }
                }
                invalidateParent();
            }
            if (animatedProgressValue >= 1 && previousDrawable != null) {
                animatedAlphaValue -= dt / 200.0f;
                if (animatedAlphaValue <= 0) {
                    animatedAlphaValue = 0.0f;
                    previousDrawable = null;
                }
                invalidateParent();
            }
        }

        public void setProgressColor(int color) {
            progressPaint.setColor(color);
        }

        public void setBackgroundColor(int color) {
            backgroundPaint.setColor(backgroundColor = color);
        }

        public void setHideCurrentDrawable(boolean value) {
            hideCurrentDrawable = value;
        }

        public void setProgress(float value, boolean animated) {
            if (!animated) {
                animatedProgressValue = value;
                animationProgressStart = value;
            } else {
                animationProgressStart = animatedProgressValue;
            }
            currentProgress = value;
            currentProgressTime = 0;

            invalidateParent();
        }

        private void invalidateParent() {
            int offset = DisplayController.dp(2);
            parent.invalidate((int) progressRect.left - offset, (int) progressRect.top - offset, (int) progressRect.right + offset * 2, (int) progressRect.bottom + offset * 2);
        }

        public void setBackground(Drawable drawable, boolean withRound, boolean animated) {
            lastUpdateTime = System.currentTimeMillis();
            if (animated && currentDrawable != drawable) {
                setProgress(1, animated);
                previousDrawable = currentDrawable;
                previousWithRound = currentWithRound;
                animatedAlphaValue = 1.0f;
            } else {
                previousDrawable = null;
                previousWithRound = false;
            }
            currentWithRound = withRound;
            currentDrawable = drawable;
            invalidateParent();
        }

        public void swapBackground(Drawable drawable) {
            currentDrawable = drawable;
        }

        public float getAlpha() {
            return previousDrawable != null || currentDrawable != null ? animatedAlphaValue : 0.0f;
        }

        public void onDraw(Canvas canvas) {
            int cx = (int)((progressRect.right - progressRect.left) / 2);
            int cy = (int)((progressRect.bottom - progressRect.top) / 2);
            int size = cx / 3;
            if(currentDrawable == null) {
                backgroundPaint.setAlpha((int)(255 * 0.5 * animatedAlphaValue));
            } else {
                backgroundPaint.setAlpha((int)(255 * 0.5));
            }
            canvas.drawCircle(cx, cy, cx, backgroundPaint);

            if (previousDrawable != null) {
                previousDrawable.setAlpha((int) (255 * animatedAlphaValue));
                //previousDrawable.setBounds((int)progressRect.left, (int)progressRect.top, (int)progressRect.right, (int)progressRect.bottom);
                previousDrawable.setBounds(cx - size, cy - size, cx + size, cy + size);
                previousDrawable.draw(canvas);
            }

            if (!hideCurrentDrawable && currentDrawable != null) {
                if (previousDrawable != null) {
                    currentDrawable.setAlpha((int)(255 * (1.0f - animatedAlphaValue)));
                } else {
                    currentDrawable.setAlpha(255);
                }
                //currentDrawable.setBounds((int)progressRect.left, (int)progressRect.top, (int)progressRect.right, (int)progressRect.bottom);
                currentDrawable.setBounds(cx - size, cy - size, cx + size, cy + size);
                currentDrawable.draw(canvas);
            }

            if (currentWithRound || previousWithRound) {
                int diff = DisplayController.dp(1);
                if (previousWithRound) {
                    progressPaint.setAlpha((int)(255 * animatedAlphaValue));
                } else {
                    progressPaint.setAlpha(255);
                }
                cicleRect.set(progressRect.left + diff, progressRect.top + diff, progressRect.right - diff, progressRect.bottom - diff);
                canvas.drawArc(cicleRect, -90 + radOffset, Math.max(4, 360 * animatedProgressValue), false, progressPaint);
                updateAnimation();
            }
        }
    }
}
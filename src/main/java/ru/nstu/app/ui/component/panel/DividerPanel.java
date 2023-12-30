/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import ru.nstu.app.controller.DisplayController;

public class DividerPanel extends Panel {

    private static Paint paint;

    public DividerPanel(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setColor(0xffd9d9d9);
            paint.setStrokeWidth(1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), DisplayController.dp(16) + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(getPaddingLeft(), DisplayController.dp(8), getWidth() - getPaddingRight(), DisplayController.dp(8), paint);
    }
}
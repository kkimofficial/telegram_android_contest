/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import ru.nstu.app.controller.DisplayController;

public class LoadingPanel extends FrameLayout {

    public LoadingPanel(Context context) {
        super(context);

        ProgressBar progressBar = new ProgressBar(context);
        addView(progressBar);
        LayoutParams layoutParams = (LayoutParams) progressBar.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(DisplayController.dp(54), MeasureSpec.EXACTLY));
    }
}
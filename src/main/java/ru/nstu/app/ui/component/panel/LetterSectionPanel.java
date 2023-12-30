/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import ru.nstu.app.controller.DisplayController;

public class LetterSectionPanel extends FrameLayout {

    private TextView textView;

    public LetterSectionPanel(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(DisplayController.dp(54), DisplayController.dp(64)));

        textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        textView.setTypeface(DisplayController.typeface());
        textView.setTextColor(0xff808080);
        textView.setGravity(Gravity.CENTER);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams)textView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        textView.setLayoutParams(layoutParams);
    }

    public void setLetter(String letter) {
        textView.setText(letter.toUpperCase());
    }

    public void setPanelHeight(int height) {
        setLayoutParams(new ViewGroup.LayoutParams(DisplayController.dp(54), height));
    }
}
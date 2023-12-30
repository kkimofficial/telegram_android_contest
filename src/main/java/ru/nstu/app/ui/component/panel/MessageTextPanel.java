package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import ru.nstu.app.android.Emoji;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;

public class MessageTextPanel extends MessagePanel {
    protected TextView textTextView;

    public MessageTextPanel(Context context) {
        super(context);

        textTextView = new TextView(context);
        textTextView.setTextColor(0xff333333);
        textTextView.setTextSize(DisplayController.sp(15));
        textTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(textTextView);
        LayoutParams layoutParams = (LayoutParams)textTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(22 + verticalOffset);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        textTextView.setLayoutParams(layoutParams);
    }

    @Override
    public void update() {
        textTextView.setText(Emoji.replaceEmoji(message.getContentText(), null, DisplayController.dp(15)));
    }
}

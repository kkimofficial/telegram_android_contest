package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;

public class SystemDatePanel extends MessagePanel {
    private TextView dateTextView;

    public SystemDatePanel(Context context) {
        super(context);

        avatarImageView.setVisibility(View.GONE);
        titleLayout.setVisibility(View.GONE);

        dateTextView = new TextView(context);
        addView(dateTextView);
        LayoutParams layoutParams = (LayoutParams)dateTextView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = DisplayController.dp(8);
        layoutParams.rightMargin = DisplayController.dp(8);
        layoutParams.topMargin = DisplayController.dp(8);
        layoutParams.bottomMargin = DisplayController.dp(8);
        dateTextView.setLayoutParams(layoutParams);
        dateTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        dateTextView.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    public void update() {
        dateTextView.setText(LocaleController.formatDate(message.getContentSystemDate(), LocaleController.DateFormat.MESSAGES_LIST));
    }
}

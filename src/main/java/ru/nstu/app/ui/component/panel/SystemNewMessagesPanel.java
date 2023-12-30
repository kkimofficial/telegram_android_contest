package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;

public class SystemNewMessagesPanel extends MessagePanel {
    private TextView newTextView;

    public SystemNewMessagesPanel(Context context) {
        super(context);

        avatarImageView.setVisibility(View.GONE);
        titleLayout.setVisibility(View.GONE);

        int color = context.getResources().getColor(R.color.system_blue_light);
        color = Color.argb(50, Color.red(color), Color.green(color), Color.blue(color));
        setBackgroundColor(color);

        LinearLayout linearLayout = new LinearLayout(context);
        addView(linearLayout);
        LayoutParams layoutParams = (LayoutParams)linearLayout.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = DisplayController.dp(8);
        layoutParams.rightMargin = DisplayController.dp(8);
        layoutParams.topMargin = DisplayController.dp(8);
        layoutParams.bottomMargin = DisplayController.dp(8);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        newTextView = new TextView(context);
        newTextView.setTypeface(Typeface.DEFAULT_BOLD);
        newTextView.setTextColor(context.getResources().getColor(R.color.system_blue_light));
        linearLayout.addView(newTextView);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_small_arrow);
        imageView.setPadding(0, DisplayController.dp(2), 0, 0);
        linearLayout.addView(imageView);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams)imageView.getLayoutParams();
        layoutParams1.leftMargin = DisplayController.dp(2);
        layoutParams1.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams1);
    }

    @Override
    public void update() {
        newTextView.setText(LocaleController.formatString(R.string.new_messages, Box.get(Box.UNREAD_COUNT)));
    }
}

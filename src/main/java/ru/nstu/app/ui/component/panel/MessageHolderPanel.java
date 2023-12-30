package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import ru.nstu.app.R;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;

public class MessageHolderPanel extends MessagePanel {
    private View holderView;
    private MessagePanel messagePanel;

    public MessageHolderPanel(Context context) {
        super(context);

        holderView = new View(context);
        addView(holderView);
        LayoutParams layoutParams = (LayoutParams)holderView.getLayoutParams();
        holderView.setBackgroundColor(context.getResources().getColor(R.color.system));
        layoutParams.width = DisplayController.dp(4);
        layoutParams.height = DisplayController.dp(42);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : 58);
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 : 16);
        layoutParams.topMargin = DisplayController.dp(24);
        holderView.setLayoutParams(layoutParams);
    }

    public void setMessagePanel(MessagePanel messagePanel) {
        this.messagePanel = messagePanel;
        addView(messagePanel);
    }

    @Override
    public void update() {
        if(message.isForward()) {
            LayoutParams layoutParams = (LayoutParams)messagePanel.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
            layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : 58);
            layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 : 16);
            layoutParams.topMargin = DisplayController.dp(24);
            messagePanel.setLayoutParams(layoutParams);
            holderView.setVisibility(View.VISIBLE);
            avatarImageView.setVisibility(View.VISIBLE);
            titleLayout.setVisibility(View.VISIBLE);
        } else {
            LayoutParams layoutParams = (LayoutParams)messagePanel.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
            layoutParams.leftMargin = 0;
            layoutParams.rightMargin = 0;
            layoutParams.topMargin = 0;
            messagePanel.setLayoutParams(layoutParams);
            holderView.setVisibility(View.GONE);
            avatarImageView.setVisibility(View.GONE);
            titleLayout.setVisibility(View.GONE);
        }
        messagePanel.setData(message);
    }
}

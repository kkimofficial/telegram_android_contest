package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.phoneformat.PhoneFormat;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.component.common.AvatarImageView;
import ru.nstu.app.ui.component.common.BackendImageView;

public class MessageContactPanel extends MessagePanel {
    protected View contactView;
    protected AvatarImageView contactAvatarImageView;
    protected TextView contactTitleTextView;
    protected TextView contactPhoneTextView;

    public MessageContactPanel(Context context) {
        super(context);

        contactView = new View(context);
        addView(contactView);
        LayoutParams layoutParams = (LayoutParams)contactView.getLayoutParams();
        contactView.setBackgroundColor(context.getResources().getColor(R.color.system_blue_light));
        layoutParams.width = DisplayController.dp(4);
        layoutParams.height = DisplayController.dp(42);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        contactView.setLayoutParams(layoutParams);

        contactAvatarImageView = new AvatarImageView(context);
        addView(contactAvatarImageView);
        layoutParams = (LayoutParams) contactAvatarImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(42);
        layoutParams.height = DisplayController.dp(42);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + 8 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + 8 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        contactAvatarImageView.setLayoutParams(layoutParams);

        contactTitleTextView = new TextView(context);
        contactTitleTextView.setLines(1);
        contactTitleTextView.setMaxLines(1);
        contactTitleTextView.setSingleLine(true);
        contactTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        contactTitleTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(contactTitleTextView);
        layoutParams = (LayoutParams) contactTitleTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + 8 + 42 + 8));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + 8 + 42 + 8) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        contactTitleTextView.setLayoutParams(layoutParams);
        contactTitleTextView.setTextColor(0xff569ace);
        contactTitleTextView.setTextSize(DisplayController.sp(15));
        contactTitleTextView.setTypeface(contactTitleTextView.getTypeface(), Typeface.BOLD);

        contactPhoneTextView = new TextView(context);
        contactPhoneTextView.setLines(1);
        contactPhoneTextView.setMaxLines(1);
        contactPhoneTextView.setSingleLine(true);
        contactPhoneTextView.setEllipsize(TextUtils.TruncateAt.END);
        contactPhoneTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(contactPhoneTextView);
        layoutParams = (LayoutParams) contactPhoneTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + 8 + 42 + 8));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + 8 + 42 + 8) : 16);
        layoutParams.topMargin = DisplayController.dp(46 + verticalOffset);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        contactPhoneTextView.setLayoutParams(layoutParams);
        contactPhoneTextView.setTextColor(0xffb2b2b2);
        contactPhoneTextView.setTextSize(DisplayController.sp(13));
    }

    @Override
    public void update() {
        final User user = UserController.getUser(message.getContentContactUserId());
        contactAvatarImageView.setInfo(user);
        if(FileController.isExists(user.getPhotoSmall())) {
            if(FileController.isCached(user.getPhotoSmall())) {
                FileController.load(contactAvatarImageView, FileController.getPath(user.getPhotoSmall()));
            } else {
                FileController.load(FileController.getId(user.getPhotoSmall()), new Callback() {
                    @Override
                    public void call(Object value) {
                        if(value instanceof TdApi.UpdateFile) {
                            FileController.load(contactAvatarImageView, FileController.getPath(user.getPhotoSmall()));
                        }
                    }
                });
            }
        }
        contactTitleTextView.setText(UserController.formatName(user.getFirstName(), user.getLastName()));
        contactPhoneTextView.setText(PhoneFormat.getInstance().format("+" + user.getPhoneNumber()));
    }

    public void updatePhone() {
        final User user = UserController.getUser(message.getContentContactUserId());
        contactPhoneTextView.setText(PhoneFormat.getInstance().format("+" + user.getPhoneNumber()));
    }
}

package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.common.AvatarImageView;

public class MessageForwardTextPanel extends MessageTextPanel {
    protected AvatarImageView forwardAvatarImageView;
    protected TextView forwardNameTextView;
    protected TextView forwardDateTextView;
    protected ImageView forwardBadgeImageView;
    protected LinearLayout forwardTitleLayout;
    protected View forwardView;

    public MessageForwardTextPanel(Context context) {
        super(context);

        forwardAvatarImageView = new AvatarImageView(context);
        addView(forwardAvatarImageView);
        LayoutParams layoutParams = (LayoutParams)forwardAvatarImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(42);
        layoutParams.height = DisplayController.dp(42);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = LocaleController.isRTL ? 0 : DisplayController.dp(8);
        layoutParams.rightMargin = LocaleController.isRTL ? DisplayController.dp(8) : 0;
        layoutParams.topMargin = 0;
        forwardAvatarImageView.setLayoutParams(layoutParams);

        forwardTitleLayout = new LinearLayout(context);
        addView(forwardTitleLayout);
        layoutParams = (LayoutParams)forwardTitleLayout.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 8 : 58);
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 : 8);
        layoutParams.topMargin = 0;
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        forwardTitleLayout.setLayoutParams(layoutParams);

        forwardNameTextView = new TextView(context);
        forwardNameTextView.setTextColor(0xff569ace);
        forwardNameTextView.setTextSize(DisplayController.sp(15));
        forwardNameTextView.setTypeface(null, Typeface.BOLD);
        forwardNameTextView.setLines(1);
        forwardNameTextView.setMaxLines(1);
        forwardNameTextView.setSingleLine(true);
        forwardNameTextView.setEllipsize(TextUtils.TruncateAt.END);
        forwardNameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        forwardTitleLayout.addView(forwardNameTextView);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) nameTextView.getLayoutParams();
        layoutParams2.width = LayoutParams.WRAP_CONTENT;
        layoutParams2.height = LayoutParams.WRAP_CONTENT;
        layoutParams2.leftMargin = 0;
        layoutParams2.rightMargin = DisplayController.dp(4);
        layoutParams2.topMargin = 0;
        forwardNameTextView.setLayoutParams(layoutParams2);

        forwardDateTextView = new TextView(context);
        forwardDateTextView.setTextColor(0xffb2b2b2);
        forwardDateTextView.setTextSize(DisplayController.sp(13));
        forwardDateTextView.setLines(1);
        forwardDateTextView.setMaxLines(1);
        forwardDateTextView.setSingleLine(true);
        forwardDateTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        forwardTitleLayout.addView(forwardDateTextView);
        layoutParams2 = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        layoutParams2.width = LayoutParams.WRAP_CONTENT;
        layoutParams2.height = LayoutParams.WRAP_CONTENT;
        layoutParams2.leftMargin = 0;
        layoutParams2.rightMargin = DisplayController.dp(4);
        layoutParams2.topMargin = 0;
        layoutParams2.weight = 1;
        forwardDateTextView.setLayoutParams(layoutParams2);

        forwardBadgeImageView = new ImageView(context);
        forwardBadgeImageView.setImageResource(R.drawable.ic_badge_blue);
        forwardTitleLayout.addView(forwardBadgeImageView);
        layoutParams2 = (LinearLayout.LayoutParams)forwardBadgeImageView.getLayoutParams();
        layoutParams2.width = DisplayController.dp(9);
        layoutParams2.height = DisplayController.dp(9);
        layoutParams2.leftMargin = 0;
        layoutParams2.rightMargin = 0;
        layoutParams2.topMargin = DisplayController.dp(7);
        forwardBadgeImageView.setLayoutParams(layoutParams2);

        forwardView = new View(context);
        addView(forwardView);
        layoutParams = (LayoutParams)forwardView.getLayoutParams();
        forwardView.setBackgroundColor(context.getResources().getColor(R.color.system_blue_light));
        layoutParams.width = DisplayController.dp(4);
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : 58);
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 : 16);
        layoutParams.topMargin = DisplayController.dp(24);
        forwardView.setLayoutParams(layoutParams);
    }

    @Override
    public void update() {
        super.update();

        Message nextMessage = Droid.activity.getMessagesCreator().getNextMessage(message);
        if(nextMessage != null && !nextMessage.isContentSystemNewMessages() && !nextMessage.isContentSystemDate() && message.getForwardFromId() == nextMessage.getForwardFromId() && nextMessage.isContentText()) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)getLayoutParams();
            layoutParams.bottomMargin = 0;
            setLayoutParams(layoutParams);
        } else {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)getLayoutParams();
            layoutParams.bottomMargin = DisplayController.dp(6);
            setLayoutParams(layoutParams);
        }

        Message previousMessage = Droid.activity.getMessagesCreator().getPreviousMessage(message);
        if(previousMessage != null && !previousMessage.isContentSystemNewMessages() && !previousMessage.isContentSystemDate() && message.getForwardFromId() == previousMessage.getForwardFromId() && previousMessage.isContentText()) {
            RecyclerView.LayoutParams layoutParams1 = (RecyclerView.LayoutParams)getLayoutParams();
            layoutParams1.topMargin = 0;
            setLayoutParams(layoutParams1);

            forwardAvatarImageView.setVisibility(View.GONE);
            forwardTitleLayout.setVisibility(View.GONE);
            LayoutParams layoutParams = (LayoutParams)avatarImageView.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(12);
            avatarImageView.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams)titleLayout.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(12);
            titleLayout.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams)forwardView.getLayoutParams();
            layoutParams.topMargin = 0;
            forwardView.setLayoutParams(layoutParams);

            layoutParams = (LayoutParams)textTextView.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(22 + 12);
            textTextView.setLayoutParams(layoutParams);
        } else {
            RecyclerView.LayoutParams layoutParams1 = (RecyclerView.LayoutParams)getLayoutParams();
            layoutParams1.topMargin = DisplayController.dp(6);
            setLayoutParams(layoutParams1);

            forwardAvatarImageView.setVisibility(View.VISIBLE);
            forwardTitleLayout.setVisibility(View.VISIBLE);
            LayoutParams layoutParams = (LayoutParams)avatarImageView.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(verticalOffset);
            avatarImageView.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams)titleLayout.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(verticalOffset);
            titleLayout.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams)forwardView.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(verticalOffset);
            forwardView.setLayoutParams(layoutParams);

            layoutParams = (LayoutParams)textTextView.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(22 + verticalOffset);
            textTextView.setLayoutParams(layoutParams);
        }

        currentUser = UserController.getUser(message.getFromId());
        forwardAvatarImageView.setInfo(currentUser);
        if(FileController.isExists(currentUser.getPhotoSmall())) {
            if(FileController.isCached(currentUser.getPhotoSmall())) {
                FileController.load(forwardAvatarImageView, FileController.getPath(currentUser.getPhotoSmall()));
            } else {
                FileController.load(FileController.getId(currentUser.getPhotoSmall()), new Callback() {
                    @Override
                    public void call(Object value) {
                        if(value instanceof TdApi.UpdateFile) {
                            FileController.load(forwardAvatarImageView, FileController.getPath(currentUser.getPhotoSmall()));
                        }
                    }
                });
            }
        }

        String name = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
        String date = LocaleController.formatDate(message.getDate(), LocaleController.DateFormat.DIALOGS_LIST);
        forwardNameTextView.setText(name);
        textPaint.setTextSize(forwardDateTextView.getTextSize());
        forwardNameTextView.setMaxWidth(DisplayController.screenWidth - DisplayController.dp(74) - (int) textPaint.measureText(date) - DisplayController.dp(9));
        forwardDateTextView.setText(date);
    }

    @Override
    public int getHorizontalOffset() {
        return 58;
    }

    @Override
    public int getVerticalOffset() {
        return 24;
    }
}

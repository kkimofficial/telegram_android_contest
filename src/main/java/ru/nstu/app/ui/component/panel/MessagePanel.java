package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.component.common.AvatarImageView;

public abstract class MessagePanel extends FrameLayout {
    protected AvatarImageView avatarImageView;
    protected User currentUser;
    protected TextView nameTextView;
    protected TextView dateTextView;
    protected Message message;
    protected ImageView badgeImageView;
    protected LinearLayout titleLayout;

    protected TextPaint textPaint;
    protected int horizontalOffset;
    protected int verticalOffset;

    protected TdApi.File avatarImageViewFile;

    public MessagePanel(Context context) {
        super(context);

        textPaint = new TextPaint();

        RecyclerView.LayoutParams layoutParams1 = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
        layoutParams1.topMargin = DisplayController.dp(6);
        layoutParams1.bottomMargin = DisplayController.dp(6);
        setLayoutParams(layoutParams1);

        horizontalOffset = getHorizontalOffset();
        verticalOffset = getVerticalOffset();

        avatarImageView = new AvatarImageView(context);
        addView(avatarImageView);
        LayoutParams layoutParams = (LayoutParams) avatarImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(42);
        layoutParams.height = DisplayController.dp(42);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = LocaleController.isRTL ? 0 : DisplayController.dp(8 + horizontalOffset);
        layoutParams.rightMargin = LocaleController.isRTL ? DisplayController.dp(8 + horizontalOffset) : 0;
        layoutParams.topMargin = DisplayController.dp(verticalOffset);
        avatarImageView.setLayoutParams(layoutParams);

        titleLayout = new LinearLayout(context);
        addView(titleLayout);
        layoutParams = (LayoutParams)titleLayout.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 8 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 8);
        layoutParams.topMargin = DisplayController.dp(verticalOffset);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        titleLayout.setLayoutParams(layoutParams);

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xff569ace);
        nameTextView.setTextSize(DisplayController.sp(15));
        nameTextView.setTypeface(null, Typeface.BOLD);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        titleLayout.addView(nameTextView);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) nameTextView.getLayoutParams();
        layoutParams2.width = LayoutParams.WRAP_CONTENT;
        layoutParams2.height = LayoutParams.WRAP_CONTENT;
        layoutParams2.leftMargin = 0;
        layoutParams2.rightMargin = DisplayController.dp(4);
        layoutParams2.topMargin = 0;
        nameTextView.setLayoutParams(layoutParams2);

        dateTextView = new TextView(context);
        dateTextView.setTextColor(0xffb2b2b2);
        dateTextView.setTextSize(DisplayController.sp(13));
        dateTextView.setLines(1);
        dateTextView.setMaxLines(1);
        dateTextView.setSingleLine(true);
        dateTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        titleLayout.addView(dateTextView);
        layoutParams2 = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        layoutParams2.width = LayoutParams.WRAP_CONTENT;
        layoutParams2.height = LayoutParams.WRAP_CONTENT;
        layoutParams2.leftMargin = 0;
        layoutParams2.rightMargin = DisplayController.dp(4);
        layoutParams2.topMargin = 0;
        layoutParams2.weight = 1;
        dateTextView.setLayoutParams(layoutParams2);

        badgeImageView = new ImageView(context);
        badgeImageView.setImageResource(R.drawable.ic_badge_blue);
        titleLayout.addView(badgeImageView);
        layoutParams2 = (LinearLayout.LayoutParams)badgeImageView.getLayoutParams();
        layoutParams2.width = DisplayController.dp(9);
        layoutParams2.height = DisplayController.dp(9);
        layoutParams2.leftMargin = 0;
        layoutParams2.rightMargin = 0;
        layoutParams2.topMargin = DisplayController.dp(7);
        badgeImageView.setLayoutParams(layoutParams2);
    }

    public void setData(Message message) {
        this.message = message;

        if(!message.isContentSystemDate() && !message.isContentSystemNewMessages()) {
            currentUser = UserController.getUser(message.isForward() && message.isContentText() ? message.getForwardFromId() : message.getFromId());
            avatarImageView.setInfo(currentUser);
            if(FileController.isExists(avatarImageViewFile = currentUser.getPhotoSmall())) {
                if(FileController.isCached(currentUser.getPhotoSmall())) {
                    FileController.load(avatarImageView, FileController.getPath(currentUser.getPhotoSmall()));
                } else {
                    FileController.load(FileController.getId(currentUser.getPhotoSmall()), new Callback() {
                        @Override
                        public void call(Object value) {
                            if(value instanceof TdApi.UpdateFile) {
                                FileController.load(avatarImageView, FileController.getPath(currentUser.getPhotoSmall()));
                            }
                        }
                    });
                }
            }

            String name = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            String date = LocaleController.formatDate(message.getDate(), LocaleController.DateFormat.DIALOGS_LIST);
            int rightOffset = DisplayController.dp(9);
            badgeImageView.setVisibility(View.VISIBLE);
            if(!message.isUnread() || !message.isOut()) {
                rightOffset = 0;
                badgeImageView.setVisibility(View.GONE);
            }
            if(message.isForward()) {
                date = LocaleController.formatDate(message.getForwardDate(), LocaleController.DateFormat.DIALOGS_LIST);
                rightOffset = DisplayController.dp(58);
                badgeImageView.setVisibility(View.GONE);
            }
            nameTextView.setText(name);
            textPaint.setTextSize(dateTextView.getTextSize());
            nameTextView.setMaxWidth(DisplayController.screenWidth - DisplayController.dp(74) - (int) textPaint.measureText(date) - rightOffset);
            dateTextView.setText(date);
        }

        update();
    }

    public abstract void update();

    public void updateBudge() {
        if(message.isContentSystemDate() || message.isContentSystemNewMessages()) {
            return;
        }
        String date = LocaleController.formatDate(message.getDate(), LocaleController.DateFormat.DIALOGS_LIST);
        int rightOffset = DisplayController.dp(9);
        badgeImageView.setVisibility(View.VISIBLE);
        if(!message.isUnread() || !message.isOut()) {
            rightOffset = 0;
            badgeImageView.setVisibility(View.GONE);
        }
        if(message.isForward()) {
            date = LocaleController.formatDate(message.getForwardDate(), LocaleController.DateFormat.DIALOGS_LIST);
            rightOffset = DisplayController.dp(58);
            badgeImageView.setVisibility(View.GONE);
        }
        textPaint.setTextSize(dateTextView.getTextSize());
        nameTextView.setMaxWidth(DisplayController.screenWidth - DisplayController.dp(74) - (int) textPaint.measureText(date) - rightOffset);
    }

    public void updateTitle() {
        if(message.isContentSystemDate() || message.isContentSystemNewMessages()) {
            return;
        }
        nameTextView.setText(UserController.formatName(currentUser.getFirstName(), currentUser.getLastName()));
    }

    public void updateAvatar() {
        if(message.isContentSystemDate() || message.isContentSystemNewMessages() || FileController.getId(avatarImageViewFile) == FileController.getId(currentUser.getPhotoSmall())) {
            return;
        }
        if(FileController.isExists(currentUser.getPhotoSmall())) {
            if(FileController.isCached(currentUser.getPhotoSmall())) {
                FileController.load(avatarImageView, FileController.getPath(currentUser.getPhotoSmall()));
            } else {
                FileController.load(FileController.getId(currentUser.getPhotoSmall()), new Callback() {
                    @Override
                    public void call(Object value) {
                        if(value instanceof TdApi.UpdateFile) {
                            FileController.load(avatarImageView, FileController.getPath(currentUser.getPhotoSmall()));
                        }
                    }
                });
            }
        }
    }

    public int getHorizontalOffset() {
        return 0;
    }

    public int getVerticalOffset() {
        return 0;
    }
}

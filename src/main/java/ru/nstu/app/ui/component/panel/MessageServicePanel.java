package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.component.common.AvatarImageView;

public class MessageServicePanel extends MessagePanel {
    private TextView serviceTextView;
    private AvatarImageView serviceAvatarImageView;

    public MessageServicePanel(Context context) {
        super(context);

        avatarImageView.setVisibility(View.GONE);
        titleLayout.setVisibility(View.GONE);

        serviceTextView = new TextView(context);
        addView(serviceTextView);
        LayoutParams layoutParams = (LayoutParams)serviceTextView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = DisplayController.dp(8);
        layoutParams.rightMargin = DisplayController.dp(8);
        layoutParams.topMargin = DisplayController.dp(0);
        serviceTextView.setLayoutParams(layoutParams);
        serviceTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        serviceTextView.setTextSize(DisplayController.sp(15));

        serviceAvatarImageView = new AvatarImageView(context);
        addView(serviceAvatarImageView);
        layoutParams = (LayoutParams)serviceAvatarImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.width = DisplayController.dp(54);
        layoutParams.height = DisplayController.dp(54);
        layoutParams.leftMargin = DisplayController.dp(8);
        layoutParams.rightMargin = DisplayController.dp(8);
        layoutParams.topMargin = DisplayController.dp(24);
        serviceAvatarImageView.setLayoutParams(layoutParams);
    }

    @Override
    public void update() {


        if(message.isContentServiceAddParticipant()) {
            String actorName = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            User user = message.getContentServiceAddParticipantUser();
            String subjectName = UserController.formatName(user.getFirstName(), user.getLastName());

            SpannableString spannableString = new SpannableString(actorName + " invited " + subjectName);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, actorName.length(), 0);
            //spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), 0, actorName.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), 0, actorName.length(), 0);

//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length(), actorName.length() + " invited ".length(), 0);
//            spannableString.setSpan(new ForegroundColorSpan(0xffb2b2b2), actorName.length(), actorName.length() + " invited ".length(), 0);

            spannableString.setSpan(new StyleSpan(Typeface.BOLD), actorName.length() + " invited ".length(), spannableString.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length() + " invited ".length(), spannableString.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), actorName.length() + " invited ".length(), spannableString.length(), 0);
            serviceTextView.setText(spannableString);
        }

        if(message.isContentServiceChangeTitle()) {
            String actorName = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            SpannableString spannableString = new SpannableString(actorName + " changed group name to \"" + message.getContentServiceChangeTitleTitle() + "\"");
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, actorName.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), 0, actorName.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), 0, actorName.length(), 0);

//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length() + 1, spannableString.length(), 0);
//            spannableString.setSpan(new ForegroundColorSpan(0xffb2b2b2), actorName.length() + 1, spannableString.length(), 0);
            serviceTextView.setText(spannableString);
        }

        if(message.isContentServiceDeleteParticipant()) {
            String actorName = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            User user = message.getContentServiceDeleteParticipantUser();
            String subjectName = UserController.formatName(user.getFirstName(), user.getLastName());

            SpannableString spannableString = new SpannableString(actorName + " kicked " + subjectName);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, actorName.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), 0, actorName.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), 0, actorName.length(), 0);

//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length(), actorName.length() + " kicked ".length(), 0);
//            spannableString.setSpan(new ForegroundColorSpan(0xffb2b2b2), actorName.length(), actorName.length() + " kicked ".length(), 0);

            spannableString.setSpan(new StyleSpan(Typeface.BOLD), actorName.length() + " kicked ".length(), spannableString.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length() + " kicked ".length(), spannableString.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), actorName.length() + " kicked ".length(), spannableString.length(), 0);
            serviceTextView.setText(spannableString);
        }

        if(message.isContentServiceDeletePhoto()) {
            String actorName = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            SpannableString spannableString = new SpannableString(actorName + " removed group photo");
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, actorName.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), 0, actorName.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), 0, actorName.length(), 0);

//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length() + 1, spannableString.length(), 0);
//            spannableString.setSpan(new ForegroundColorSpan(0xffb2b2b2), actorName.length() + 1, spannableString.length(), 0);
            serviceTextView.setText(spannableString);
        }

        if(message.isContentServiceGroupCreate()) {
            String actorName = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            SpannableString spannableString = new SpannableString(actorName + " created the group");
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, actorName.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), 0, actorName.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), 0, actorName.length(), 0);

//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length() + 1, spannableString.length(), 0);
//            spannableString.setSpan(new ForegroundColorSpan(0xffb2b2b2), actorName.length() + 1, spannableString.length(), 0);
            serviceTextView.setText(spannableString);
        }

        if(message.isContentServiceChangePhoto()) {
            String actorName = UserController.formatName(currentUser.getFirstName(), currentUser.getLastName());
            SpannableString spannableString = new SpannableString(actorName + " changed group photo");
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, actorName.length(), 0);
//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), 0, actorName.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(0xff569ace), 0, actorName.length(), 0);

//            spannableString.setSpan(new AbsoluteSizeSpan(DisplayController.sp(15)), actorName.length() + 1, spannableString.length(), 0);
//            spannableString.setSpan(new ForegroundColorSpan(0xffb2b2b2), actorName.length() + 1, spannableString.length(), 0);
            serviceTextView.setText(spannableString);

            serviceAvatarImageView.setVisibility(View.VISIBLE);

            final int tag = FileController.getId(message.getContentServiceChangePhotoFile());
            setTag(R.id.TAG_0, tag);

            if(FileController.isCached(message.getContentServiceChangePhotoFile())) {
                FileController.load(serviceAvatarImageView, FileController.getPath(message.getContentServiceChangePhotoFile()));
            } else {
                final WeakReference<AvatarImageView> weakReferenceImageView = new WeakReference<AvatarImageView>(serviceAvatarImageView);
                final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(message.getContentServiceChangePhotoFile());
                FileController.load(FileController.getId(message.getContentServiceChangePhotoFile()), new Callback() {
                    @Override
                    public void call(final Object value) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                if (!MessageServicePanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                    return;
                                }
                                if (value instanceof TdApi.UpdateFile) {
                                    if (weakReferenceImageView.get() != null && weakReferenceFile.get() != null) {
                                        FileController.load(weakReferenceImageView.get(), FileController.getPath(weakReferenceFile.get()));
                                    }
                                }
                            }
                        });
                    }
                });
            }
        } else {
            serviceAvatarImageView.setVisibility(View.GONE);
        }


    }
}

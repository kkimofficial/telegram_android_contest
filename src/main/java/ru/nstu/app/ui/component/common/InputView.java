package ru.nstu.app.ui.component.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Emoji;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.android.Notification;
import ru.nstu.app.api.action.SendMessagePhotoAction;
import ru.nstu.app.api.action.SendMessageTextAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.controller.ViewController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.animationcompact.AnimatorListenerAdapterProxy;
import ru.nstu.app.ui.animationcompact.AnimatorSetProxy;
import ru.nstu.app.ui.animationcompact.ObjectAnimatorProxy;
import ru.nstu.app.ui.animationcompact.ViewProxy;
import ru.nstu.app.ui.component.adapter.GalleryPhotosAdapter;
import ru.nstu.app.ui.component.panel.DividerPanel;
import ru.nstu.app.ui.component.panel.GalleryPhotoPanel;
import ru.nstu.app.ui.component.panel.TextPanel;
import ru.nstu.app.ui.component.viewholder.GalleryPhotoViewHolder;

public abstract class InputView extends FrameLayout {
    private ImageView emojiImageView;
    private EditText messageEditText;
    private ImageView attachImageView;
    private PopupWindow emojiPopup;
    private PopupWindow attachPopup;
    private EmojiView emojiView;
    private ImageView sendImageView;

    private AnimatorSetProxy runningAnimation;
    private AnimatorSetProxy runningAnimation2;
    private int runningAnimationType;

    private int keyboardHeightPortrait;
    private int keyboardHeightLand;

    private boolean sendByEnter;

    public InputView(Context context) {
        super(context);
        buildEmojiImageView(context);
        buildMessageEditText(context);
        buildAttachImageView(context);
        buildSendImageView(context);
        buildDividerView(context);
    }

    private boolean emojiPopupVisible;
    private boolean keyboardVisible;

    private void buildDividerView(Context context) {
        View dividerView = new View(context);
        dividerView.setBackgroundColor(0x11000000);
        addView(dividerView);
        LayoutParams layoutParams = (LayoutParams)dividerView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = 1;
        layoutParams.gravity = Gravity.TOP;
        dividerView.setLayoutParams(layoutParams);
    }

    private void buildEmojiImageView(final Context context) {
        emojiImageView = new ImageView(context);
        emojiImageView.setImageResource(R.drawable.ic_smiles);
        emojiImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        emojiImageView.setPadding(DisplayController.dp(4), DisplayController.dp(1), 0, 0);
        addView(emojiImageView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)emojiImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(48);
        layoutParams.height = DisplayController.dp(48);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        emojiImageView.setLayoutParams(layoutParams);

        emojiImageView.setOnClickListener(new OnClickListener() {
            private boolean pressed;

            @Override
            public void onClick(View v) {
                synchronized (this) {
                    if (pressed) {
                        return;
                    }
                    pressed = true;
                }
                if (emojiPopupVisible) {

                    hideEmojiPopup(context);
                } else {

                    keyboardVisible = false;
                    showEmojiPopup(context);
                }
                pressed = false;
            }
        });

        keyboardHeightPortrait = (Integer)Droid.load(Droid.PREFERENCES_COMMON, Droid.PREFERENCES_COMMON_KEYBOARD_HEIGHT_PORTRAIT, Integer.valueOf(DisplayController.dp(230)));
        keyboardHeightLand = (Integer)Droid.load(Droid.PREFERENCES_COMMON, Droid.PREFERENCES_COMMON_KEYBOARD_HEIGHT_LAND, Integer.valueOf(DisplayController.dp(230)));
    }

    private void buildMessageEditText(final Context context) {
        messageEditText = new EditText(context);
        messageEditText.setHint(LocaleController.getString(R.string.type_message));
        messageEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        messageEditText.setInputType(messageEditText.getInputType() | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        messageEditText.setSingleLine(false);
        messageEditText.setMaxLines(4);
        messageEditText.setTextSize(DisplayController.sp(18));
        messageEditText.setGravity(Gravity.BOTTOM);
        messageEditText.setPadding(0, DisplayController.dp(11), 0, DisplayController.dp(12));
        if(Build.VERSION.SDK_INT >= 16) {
            messageEditText.setBackground(null);
        } else {
            messageEditText.setBackgroundDrawable(null);
        }
        messageEditText.setTextColor(0xff000000);
        messageEditText.setHintTextColor(0xffb2b2b2);
        addView(messageEditText);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)messageEditText.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.leftMargin = DisplayController.dp(52);
        layoutParams.rightMargin = DisplayController.dp(52);
        layoutParams.topMargin = DisplayController.dp(1);
        messageEditText.setLayoutParams(layoutParams);
        messageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiPopup != null) {
                    keyboardVisible = true;
                    hideEmojiPopup(context);
                }
            }
        });
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String message = trim(charSequence.toString());
                updateSendImageView(context);

//                if (delegate != null) {
//                    if (before > count || count > 1) {
//                        messageWebPageSearch = true;
//                    }
//                    delegate.onTextChanged(charSequence, before > count || count > 1);
//                }
//
//                if (message.length() != 0 && lastTypingTimeSend < System.currentTimeMillis() - 5000 && !ignoreTextChange) {
//                    int currentTime = LocaleController.getCurrentTime();
//                    User currentUser = null;
//                    if ((int) dialog_id > 0) {
//                        currentUser = UserController.getUser((int) dialog_id);
//                    }
//                    if (currentUser != null && (currentUser.getId() == UserController.userId || currentUser.getUserStatusOnline() != null && currentUser.getUserStatusOnline().expires < currentTime)) {
//                        return;
//                    }
//                    lastTypingTimeSend = System.currentTimeMillis();
//                    if (delegate != null) {
//                        delegate.needSendTyping();
//                    }
//                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (sendByEnter && editable.length() > 0 && editable.charAt(editable.length() - 1) == '\n') {
                    sendMessage();
                }
                int i = 0;
                ImageSpan[] arrayOfImageSpan = editable.getSpans(0, editable.length(), ImageSpan.class);
                int j = arrayOfImageSpan.length;
                while (true) {
                    if (i >= j) {
                        Emoji.replaceEmoji(editable, messageEditText.getPaint().getFontMetricsInt(), DisplayController.dp(20));
                        return;
                    }
                    editable.removeSpan(arrayOfImageSpan[i]);
                    i++;
                }
            }
        });
    }

    private void sendMessage() {
        Dialog dialog = (Dialog)Box.get(Box.DIALOG);
        String message = trim(messageEditText.getText().toString());
        if(message.length() > 0) {
            Droid.doAction(new SendMessageTextAction(dialog.getId(), message));
            messageEditText.setText("");
        }
    }

    private void buildAttachImageView(final Context context) {
        attachImageView = new ImageView(context);
        attachImageView.setImageResource(R.drawable.ic_attach);
        attachImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        attachImageView.setPadding(0, DisplayController.dp(3), 0, DisplayController.dp(4));
        addView(attachImageView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)attachImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(48);
        layoutParams.height = DisplayController.dp(48);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        attachImageView.setLayoutParams(layoutParams);

        attachImageView.setOnClickListener(new OnClickListener() {
            private boolean pressed;

            @Override
            public void onClick(View v) {
                synchronized (this) {
                    if (pressed) {
                        return;
                    }
                    showAttachPopup(context);
                    pressed = true;
                }

                pressed = false;
            }
        });
    }

    private void buildSendImageView(final Context context) {
        sendImageView = new ImageView(context);
        sendImageView.setImageResource(R.drawable.ic_send);
        sendImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sendImageView.setPadding(0, DisplayController.dp(3), 0, DisplayController.dp(4));
        addView(sendImageView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)sendImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(48);
        layoutParams.height = DisplayController.dp(48);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        sendImageView.setLayoutParams(layoutParams);
        sendImageView.setVisibility(View.GONE);

        sendImageView.setOnClickListener(new OnClickListener() {
            private boolean pressed;

            @Override
            public void onClick(View v) {
                synchronized (this) {
                    if (pressed) {
                        return;
                    }
                    pressed = true;
                }
                sendMessage();
                pressed = false;
            }
        });
    }

    private String trim(String src) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    private void updateSendImageView(Context context) {
        boolean animated = true;
        boolean forceShowSendButton = false;
        String message = trim(messageEditText.getText().toString());
        if (message.length() > 0 || forceShowSendButton) {
//            if (audioSendButton.getVisibility() == View.VISIBLE) {
            if (attachImageView.getVisibility() == View.VISIBLE) {
                if (animated) {
                    if (runningAnimationType == 1) {
                        return;
                    }
                    if (runningAnimation != null) {
                        runningAnimation.cancel();
                        runningAnimation = null;
                    }
                    if (runningAnimation2 != null) {
                        runningAnimation2.cancel();
                        runningAnimation2 = null;
                    }

                    if (attachImageView != null) {
                        runningAnimation2 = new AnimatorSetProxy();
                        runningAnimation2.playTogether(
                                ObjectAnimatorProxy.ofFloat(attachImageView, "alpha", 0.0f),
                                ObjectAnimatorProxy.ofFloat(attachImageView, "scaleX", 0.0f)
                        );
                        runningAnimation2.setDuration(100);
                        runningAnimation2.addListener(new AnimatorListenerAdapterProxy() {
                            @Override
                            public void onAnimationEnd(Object animation) {
                                if (runningAnimation2.equals(animation)) {
                                    attachImageView.setVisibility(View.GONE);
                                    attachImageView.clearAnimation();
                                }
                            }
                        });
                        runningAnimation2.start();

                        if (messageEditText != null) {
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageEditText.getLayoutParams();
                            layoutParams.rightMargin = DisplayController.dp(0);
                            messageEditText.setLayoutParams(layoutParams);
                        }

//                        delegate.onAttachButtonHidden();
                    }

                    sendImageView.setVisibility(View.VISIBLE);
                    runningAnimation = new AnimatorSetProxy();
                    runningAnimationType = 1;

                    runningAnimation.playTogether(
//                            ObjectAnimatorProxy.ofFloat(audioSendButton, "scaleX", 0.1f),
//                            ObjectAnimatorProxy.ofFloat(audioSendButton, "scaleY", 0.1f),
//                            ObjectAnimatorProxy.ofFloat(audioSendButton, "alpha", 0.0f),
                            ObjectAnimatorProxy.ofFloat(sendImageView, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(sendImageView, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(sendImageView, "alpha", 1.0f)
                    );

                    runningAnimation.setDuration(150);
                    runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            if (runningAnimation.equals(animation)) {
                                sendImageView.setVisibility(View.VISIBLE);
//                                audioSendButton.setVisibility(View.GONE);
//                                audioSendButton.clearAnimation();
                                runningAnimation = null;
                                runningAnimationType = 0;
                            }
                        }
                    });
                    runningAnimation.start();
                } else {
//                    ViewProxy.setScaleX(audioSendButton, 0.1f);
//                    ViewProxy.setScaleY(audioSendButton, 0.1f);
//                    ViewProxy.setAlpha(audioSendButton, 0.0f);
                    ViewProxy.setScaleX(sendImageView, 1.0f);
                    ViewProxy.setScaleY(sendImageView, 1.0f);
                    ViewProxy.setAlpha(sendImageView, 1.0f);
                    sendImageView.setVisibility(View.VISIBLE);
//                    audioSendButton.setVisibility(View.GONE);
//                    audioSendButton.clearAnimation();
                    if (attachImageView != null) {
                        attachImageView.setVisibility(View.GONE);
                        attachImageView.clearAnimation();
//                        delegate.onAttachButtonHidden();
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageEditText.getLayoutParams();
                        layoutParams.rightMargin = DisplayController.dp(0);
                        messageEditText.setLayoutParams(layoutParams);
                    }
                }
            }
        } else if (sendImageView.getVisibility() == View.VISIBLE) {
            if (animated) {
                if (runningAnimationType == 2) {
                    return;
                }

                if (runningAnimation != null) {
                    runningAnimation.cancel();
                    runningAnimation = null;
                }
                if (runningAnimation2 != null) {
                    runningAnimation2.cancel();
                    runningAnimation2 = null;
                }

                if (attachImageView != null) {
                    attachImageView.setVisibility(View.VISIBLE);
                    runningAnimation2 = new AnimatorSetProxy();
                    runningAnimation2.playTogether(
                            ObjectAnimatorProxy.ofFloat(attachImageView, "alpha", 1.0f),
                            ObjectAnimatorProxy.ofFloat(attachImageView, "scaleX", 1.0f)
                    );
                    runningAnimation2.setDuration(100);
                    runningAnimation2.start();

                    if (messageEditText != null) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageEditText.getLayoutParams();
                        layoutParams.rightMargin = DisplayController.dp(50);
                        messageEditText.setLayoutParams(layoutParams);
                    }

//                    delegate.onAttachButtonShow();
                }

//                audioSendButton.setVisibility(View.VISIBLE);
                runningAnimation = new AnimatorSetProxy();
                runningAnimationType = 2;

                runningAnimation.playTogether(
                        ObjectAnimatorProxy.ofFloat(sendImageView, "scaleX", 0.1f),
                        ObjectAnimatorProxy.ofFloat(sendImageView, "scaleY", 0.1f),
                        ObjectAnimatorProxy.ofFloat(sendImageView, "alpha", 0.0f)//,
//                        ObjectAnimatorProxy.ofFloat(audioSendButton, "scaleX", 1.0f),
//                        ObjectAnimatorProxy.ofFloat(audioSendButton, "scaleY", 1.0f),
//                        ObjectAnimatorProxy.ofFloat(audioSendButton, "alpha", 1.0f)
                );

                runningAnimation.setDuration(150);
                runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (runningAnimation.equals(animation)) {
                            sendImageView.setVisibility(View.GONE);
                            sendImageView.clearAnimation();
//                            audioSendButton.setVisibility(View.VISIBLE);
                            runningAnimation = null;
                            runningAnimationType = 0;
                        }
                    }
                });
                runningAnimation.start();
            } else {
                ViewProxy.setScaleX(sendImageView, 0.1f);
                ViewProxy.setScaleY(sendImageView, 0.1f);
                ViewProxy.setAlpha(sendImageView, 0.0f);
//                ViewProxy.setScaleX(audioSendButton, 1.0f);
//                ViewProxy.setScaleY(audioSendButton, 1.0f);
//                ViewProxy.setAlpha(audioSendButton, 1.0f);
                sendImageView.setVisibility(View.GONE);
                sendImageView.clearAnimation();
//                audioSendButton.setVisibility(View.VISIBLE);
                if (attachImageView != null) {
//                    delegate.onAttachButtonShow();
                    attachImageView.setVisibility(View.VISIBLE);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageEditText.getLayoutParams();
                    layoutParams.rightMargin = DisplayController.dp(50);
                    messageEditText.setLayoutParams(layoutParams);
                }
            }
        }
    }

    private void showEmojiPopup(Context context) {
        DisplayController.showKeyboard(messageEditText);
        if(emojiView == null) {
            emojiView = new EmojiView(context)  {
                @Override
                public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && emojiPopup != null && emojiPopup.isShowing()) {
                        emojiPopup.dismiss();
                    }
                    return super.dispatchKeyEvent(keyEvent);
                }
            };
            emojiView.setListener(new EmojiView.Listener() {
                public void onBackspace() {
                    messageEditText.dispatchKeyEvent(new KeyEvent(0, 67));
                }

                public void onEmojiSelected(String symbol) {
                    int i = messageEditText.getSelectionEnd();
                    if (i < 0) {
                        i = 0;
                    }
                    try {
                        CharSequence localCharSequence = Emoji.replaceEmoji(symbol, messageEditText.getPaint().getFontMetricsInt(), DisplayController.dp(20));
                        messageEditText.setText(messageEditText.getText().insert(i, localCharSequence));
                        int j = i + localCharSequence.length();
                        messageEditText.setSelection(j, j);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            });

        }
        emojiPopup = new PopupWindow(emojiView);
        emojiPopup.setAnimationStyle(R.style.SlidePopupAnimation);

        if(DisplayController.isPortraitOrientation()) {
            emojiPopup.setHeight(keyboardHeightPortrait);
        } else {
            emojiPopup.setHeight(keyboardHeightLand);
        }

        emojiPopup.setWidth(DisplayController.screenWidth);

        emojiPopup.showAtLocation(Droid.activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        emojiPopupVisible = true;
        emojiImageView.setImageResource(R.drawable.ic_msg_panel_kb);
    }

    public boolean hideEmojiPopup(Context context) {
        if(emojiPopup == null || emojiPopupVisible == false) {
            return false;
        }
        emojiPopup.dismiss();
        emojiPopupVisible = false;
        emojiImageView.setImageResource(R.drawable.ic_smiles);
        return true;
    }

    public boolean updateEmojiPopup(int height) {
        if(DisplayController.isPortraitOrientation()) {
            keyboardHeightPortrait = height;
            Droid.save(Droid.PREFERENCES_COMMON, Droid.PREFERENCES_COMMON_KEYBOARD_HEIGHT_PORTRAIT, Integer.valueOf(height));
        } else {
            keyboardHeightLand = height;
            Droid.save(Droid.PREFERENCES_COMMON, Droid.PREFERENCES_COMMON_KEYBOARD_HEIGHT_LAND, Integer.valueOf(height));
        }

        if(emojiPopup == null) {
            return false;
        }
        emojiPopup.update(DisplayController.screenWidth, height);
        return true;
    }

    public void showAttachPopup(final Context context) {
        DisplayController.hideKeyboard(messageEditText);
        hideEmojiPopup(context);
        final int ATTACH_CONTENT_LAYOUT_ID = Integer.MAX_VALUE - 12;

        attachPopup = new PopupWindow();

        RelativeLayout attachLayout = new RelativeLayout(context) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && attachPopup != null && attachPopup.isShowing()) {
                    attachPopup.dismiss();
                }
                return super.dispatchKeyEvent(keyEvent);
            }
        };

        attachPopup.setContentView(attachLayout);
        attachPopup.setFocusable(true);
        attachPopup.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);
        attachPopup.setHeight(DisplayController.screenHeight);
        attachPopup.setWidth(DisplayController.screenWidth);
        attachPopup.showAtLocation(Droid.activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);

        FrameLayout attachTopLayout = new FrameLayout(context);
        attachTopLayout.setBackgroundColor(0x90000000);
        attachLayout.addView(attachTopLayout);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)attachTopLayout.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ABOVE, ATTACH_CONTENT_LAYOUT_ID);
        attachTopLayout.setLayoutParams(layoutParams);
        attachTopLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attachPopup.dismiss();
            }
        });

        LinearLayout attachContentLayout = new LinearLayout(context);
        attachContentLayout.setId(ATTACH_CONTENT_LAYOUT_ID);
        attachContentLayout.setOrientation(LinearLayout.VERTICAL);
        attachContentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
        attachLayout.addView(attachContentLayout);
        layoutParams = (RelativeLayout.LayoutParams)attachContentLayout.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        attachContentLayout.setLayoutParams(layoutParams);

        TextPanel takePhotoPanel = new TextPanel(context);
        takePhotoPanel.setTextAndIcon(LocaleController.getString(R.string.take_photo), R.drawable.ic_attach_photo);
        attachContentLayout.addView(takePhotoPanel);
        takePhotoPanel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
                Droid.activity.startActivityForResult(cameraIntent, Droid.ACTIVITY_CODE_TAKE_PHOTO);
            }
        });

        final TextPanel chooseSendPanel = new TextPanel(context);
        chooseSendPanel.setTextAndIcon(LocaleController.getString(R.string.choose_from_gallery), R.drawable.ic_attach_gallery);
        attachContentLayout.addView(chooseSendPanel);

        FrameLayout photoGrid = new FrameLayout(context);
        attachContentLayout.addView(photoGrid, 0);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams)photoGrid.getLayoutParams();
        layoutParams1.height = DisplayController.dp(108);
        photoGrid.setLayoutParams(layoutParams1);
        String[] projections = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        List<String> paths = new ArrayList<String>();
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor cursor = Droid.activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projections, null, null, orderBy);
        if(cursor != null) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            for(int i = cursor.getCount() - 1; i >= 0 ; i--) {
                cursor.moveToPosition(i);
                String path = cursor.getString(columnIndex);
                paths.add(path);
            }
            cursor.close();
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        final GalleryPhotosAdapter galleryPhotosAdapter = new GalleryPhotosAdapter(paths) {
            @Override
            public GalleryPhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                return new GalleryPhotoViewHolder(new GalleryPhotoPanel(viewGroup.getContext()) {
                    @Override
                    public void onClick() {
                        if(getSelectedItemCount() == 0) {
                            chooseSendPanel.setTextAndIcon(LocaleController.getString(R.string.choose_from_gallery), R.drawable.ic_attach_gallery);
                        } else if(getSelectedItemCount() == 1) {
                            chooseSendPanel.setTextAndIcon(LocaleController.getString(R.string.send_photo), R.drawable.ic_attach_gallery);
                        } else {
                            chooseSendPanel.setTextAndIcon(LocaleController.formatString(R.string.send_photos, getSelectedItemCount()), R.drawable.ic_attach_gallery);
                        }
                    }
                });
            }
        };
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(galleryPhotosAdapter);
        recyclerView.scrollToPosition(0);
        photoGrid.addView(recyclerView);

        chooseSendPanel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (galleryPhotosAdapter.getSelectedItemCount() == 0) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                    galleryIntent.setType("image/*");
                    Droid.activity.startActivityForResult(galleryIntent, Droid.ACTIVITY_CODE_PEEK_PHOTO);
                } else {
                    attachPopup.dismiss();
                    Dialog dialog = (Dialog) Box.get(Box.DIALOG);
                    for (String path : galleryPhotosAdapter.getSelectedItems()) {
                        Droid.doAction(new SendMessagePhotoAction(dialog.getId(), path));
                    }
                }
            }
        });

        View dividerView = new View(context);
        dividerView.setBackgroundColor(0x11000000);
        attachContentLayout.addView(dividerView, 1);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams)dividerView.getLayoutParams();
        layoutParams2.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams2.height = 1;
        dividerView.setLayoutParams(layoutParams2);
    }

    public void processNotification(Notification notification) {
        if(notification == Notification.BACK_PRESSED) {
            hideEmojiPopup(Droid.activity);
        } else if(notification == Notification.PEEK_SUCCESS) {
            attachPopup.dismiss();
        }
    }
}

/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Emoji;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.android.ImageReceiver;
import ru.nstu.app.android.phoneformat.PhoneFormat;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Chat;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.component.common.AvatarImageView;

public class DialogPanel extends Panel {

    private static TextPaint namePaint;
    private static TextPaint nameEncryptedPaint;
    private static TextPaint nameUnknownPaint;
    private static TextPaint messagePaint;
    private static TextPaint messagePrintingPaint;
    private static TextPaint timePaint;
    private static TextPaint countPaint;

    private static Drawable checkDrawable;
    private static Drawable halfCheckDrawable;
    private static Drawable clockDrawable;
    private static Drawable errorDrawable;
    private static Drawable lockDrawable;
    private static Drawable countDrawable;
    private static Drawable groupDrawable;
    private static Drawable broadcastDrawable;
    private static Drawable muteDrawable;

    private static Paint linePaint;

    private long currentDialogId;
    private boolean isDialogCell;
    private int lastMessageDate;
    private int unreadCount;
    private boolean lastUnreadState;
    private int lastSendState;
    private boolean dialogMuted;
    private Message message;
    private int index;
    private boolean isServerOnly;

    private ImageReceiver avatarImage;
    private AvatarImageView.AvatarDrawable avatarDrawable;

    private User user = null;
    private Chat chat = null;
    private CharSequence lastPrintString = null;

    public boolean useSeparator = false;


    private int nameLeft;
    private StaticLayout nameLayout;
    private boolean drawNameLock;
    private boolean drawNameGroup;
    private boolean drawNameBroadcast;
    private int nameMuteLeft;
    private int nameLockLeft;
    private int nameLockTop;

    private int timeLeft;
    private int timeTop = DisplayController.dp(17);
    private StaticLayout timeLayout;

    private boolean drawCheck1;
    private boolean drawCheck2;
    private boolean drawClock;
    private int checkDrawLeft;
    private int checkDrawTop = DisplayController.dp(18);
    private int halfCheckDrawLeft;

    private int messageTop = DisplayController.dp(40);
    private int messageLeft;
    private StaticLayout messageLayout;

    private boolean drawError;
    private int errorTop = DisplayController.dp(39);
    private int errorLeft;

    private boolean drawCount;
    private int countTop = DisplayController.dp(39);
    private int countLeft;
    private int countWidth;
    private StaticLayout countLayout;

    private int avatarTop = DisplayController.dp(10);

    private void init() {
        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(DisplayController.sp(17));
            namePaint.setColor(0xff212121);
            namePaint.setTypeface(DisplayController.typeface());

            nameEncryptedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            nameEncryptedPaint.setTextSize(DisplayController.sp(17));
            nameEncryptedPaint.setColor(0xff00a60e);
            nameEncryptedPaint.setTypeface(DisplayController.typeface());

            nameUnknownPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            nameUnknownPaint.setTextSize(DisplayController.sp(17));
            nameUnknownPaint.setColor(0xff4d83b3);
            nameUnknownPaint.setTypeface(DisplayController.typeface());

            messagePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePaint.setTextSize(DisplayController.sp(16));
            messagePaint.setColor(0xff8f8f8f);
            messagePaint.linkColor = 0xff8f8f8f;

            linePaint = new Paint();
            linePaint.setColor(0xffdcdcdc);

            messagePrintingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePrintingPaint.setTextSize(DisplayController.sp(16));
            messagePrintingPaint.setColor(0xff4d83b3);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(DisplayController.sp(13));
            timePaint.setColor(0xff999999);

            countPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            countPaint.setTextSize(DisplayController.sp(13));
            countPaint.setColor(0xffffffff);
            countPaint.setTypeface(DisplayController.typeface());

            lockDrawable = getResources().getDrawable(R.drawable.list_secret);
            checkDrawable = getResources().getDrawable(R.drawable.ic_badge);//TODO: R.drawable.dialogs_check);
            halfCheckDrawable = getResources().getDrawable(R.drawable.dialogs_halfcheck);
            clockDrawable = getResources().getDrawable(R.drawable.ic_clock);
            errorDrawable = getResources().getDrawable(R.drawable.dialogs_warning);
            countDrawable = getResources().getDrawable(R.drawable.dialogs_badge);
            groupDrawable = getResources().getDrawable(R.drawable.list_group);
            broadcastDrawable = getResources().getDrawable(R.drawable.list_broadcast);
            muteDrawable = getResources().getDrawable(R.drawable.ic_mute_blue);
        }
    }

    public DialogPanel(Context context) {
        super(context);
        init();
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(DisplayController.dp(26));
        avatarDrawable = new AvatarImageView.AvatarDrawable();
    }

    public void setDialog(Dialog dialog, int i, boolean server) {
        currentDialogId = dialog.getId();
        isDialogCell = true;
        index = i;
        isServerOnly = server;
        update(0);
    }

    public void setDialog(long dialog_id, Message message, int date) {
        currentDialogId = dialog_id;
        this.message = message;
        isDialogCell = false;
        lastMessageDate = date;
        unreadCount = 0;
        lastUnreadState = message != null && message.isUnread();
        if (this.message != null) {
            lastSendState = this.message.getSendState();
        }
        update(0);
    }

    public long getDialogId() {
        return currentDialogId;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (avatarImage != null) {
            avatarImage.clearImage();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), DisplayController.dp(72));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (currentDialogId == 0) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        if (changed) {
            buildLayout();
        }
    }

    public void buildLayout() {
        String nameString = "";
        String timeString = "";
        String countString = null;
        CharSequence messageString = "";
        CharSequence printingString = null;
        if (isDialogCell) {
            printingString = MessageController.printingStrings.get(currentDialogId);
        }
        TextPaint currentNamePaint = namePaint;
        TextPaint currentMessagePaint = messagePaint;
        boolean checkMessage = true;

        drawNameGroup = false;
        drawNameBroadcast = false;
        drawNameLock = false;

        if (chat != null) {
//            if (chat.getId() < 0) {
//                drawNameBroadcast = true;
//                nameLockTop = DisplayController.dp(16.5f);
//            } else {
                drawNameGroup = true;
                nameLockTop = DisplayController.dp(17.5f);
//            }

            if (!LocaleController.isRTL) {
                nameLockLeft = DisplayController.dp(DisplayController.leftBaseline);
                nameLeft = DisplayController.dp(DisplayController.leftBaseline + 4) + (drawNameGroup ? groupDrawable.getIntrinsicWidth() : broadcastDrawable.getIntrinsicWidth());
            } else {
                nameLockLeft = getMeasuredWidth() - DisplayController.dp(DisplayController.leftBaseline) - (drawNameGroup ? groupDrawable.getIntrinsicWidth() : broadcastDrawable.getIntrinsicWidth());
                nameLeft = DisplayController.dp(14);
            }
        } else {
            if (!LocaleController.isRTL) {
                nameLeft = DisplayController.dp(DisplayController.leftBaseline);
            } else {
                nameLeft = DisplayController.dp(14);
            }
        }

        if (message == null) {
            if (printingString != null) {
                lastPrintString = messageString = printingString;
                currentMessagePaint = messagePrintingPaint;
            } else {
                lastPrintString = null;
            }
            if (lastMessageDate != 0) {
                timeString = LocaleController.formatDate(lastMessageDate, LocaleController.DateFormat.DIALOGS_LIST);
            }
            drawCheck1 = false;
            drawCheck2 = false;
            drawClock = false;
            drawCount = false;
            drawError = false;
        } else {
            User fromUser = UserController.getUser(message.getFromId());

            if (lastMessageDate != 0) {
                timeString = LocaleController.formatDate(lastMessageDate, LocaleController.DateFormat.DIALOGS_LIST);
            } else {
                //timeString = LocaleController.formatDate(message.getDate(), LocaleController.DateFormat.DIALOGS_LIST);
            }
            if (printingString != null) {
                lastPrintString = messageString = printingString;
                currentMessagePaint = messagePrintingPaint;
            } else {
                lastPrintString = null;
                if (message.isContentService()) {
                    messageString = message.getDescription();
                    currentMessagePaint = messagePrintingPaint;
                } else {
                    if (chat != null && chat.getId() < 0) {
                        String name = "";
                        if (message.isOut()) {
                            name = LocaleController.getString(R.string.from_you);
                        } else {
                            if (fromUser != null) {
                                if (fromUser.getFirstName().length() > 0) {
                                    name = fromUser.getFirstName();
                                } else {
                                    name = fromUser.getLastName();
                                }
                            }
                        }
                        checkMessage = false;
                        if (!message.isMediaEmpty()) {//(message.messageOwner.media != null && !message.isMediaEmpty()) {
                            currentMessagePaint = messagePrintingPaint;
                            messageString = Emoji.replaceEmoji(LocaleController.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff4d83b3>%s</c>", name, message.getDescription())), messagePaint.getFontMetricsInt(), DisplayController.dp(20));
                        } else {
                            if (message.getDescription() != null) {
                                String mess = message.getDescription().toString();
                                if (mess.length() > 150) {
                                    mess = mess.substring(0, 150);
                                }
                                mess = mess.replace("\n", " ");
                                messageString = Emoji.replaceEmoji(LocaleController.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff808080>%s</c>", name, mess.replace("<", "&lt;").replace(">", "&gt;"))), messagePaint.getFontMetricsInt(), DisplayController.dp(20));
                            }
                        }
                    } else {
                        messageString = message.getDescription();
                        if (!message.isMediaEmpty()) {//(message.messageOwner.media != null && !message.isMediaEmpty()) {
                            currentMessagePaint = messagePrintingPaint;
                        }
                    }
                }
            }

            if (unreadCount != 0) {
                drawCount = true;
                countString = String.format("%d", unreadCount);
            } else {
                drawCount = false;
            }

            if (message.isOut()) {
                if (message.isSending()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (message.isSendError()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                    drawCount = false;
                } else if (message.isSent()) {
                    if (!message.isUnread()) {
                        drawCheck1 = false;//TODO: true;
                        drawCheck2 = false;//TODO: true;
                    } else {
                        drawCheck1 = false;
                        drawCheck2 = true;
                    }
                    drawClock = false;
                    drawError = false;
                }
            } else {
                drawCheck1 = false;
                drawCheck2 = false;
                drawClock = false;
                drawError = false;
            }
        }

        int timeWidth = (int) Math.ceil(timePaint.measureText(timeString));
        timeLayout = new StaticLayout(timeString, timePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        if (!LocaleController.isRTL) {
            timeLeft = getMeasuredWidth() - DisplayController.dp(15) - timeWidth;
        } else {
            timeLeft = DisplayController.dp(15);
        }

        if (chat != null) {
            nameString = chat.getTitle();
        } else if (user != null) {
            if (user.getId() / 1000 != 777 && user.getId() / 1000 != 333 && UserController.contactsMap.get(user.getId()) == null) {
                if (UserController.contactsMap.size() == 0 && (!UserController.contactsEndReached || UserController.loadingContacts)) {
                    nameString = UserController.formatName(user.getFirstName(), user.getLastName());
                } else {
                    if (user.getPhoneNumber() != null && user.getPhoneNumber().length() != 0) {
                        nameString = PhoneFormat.getInstance().format("+" + user.getPhoneNumber());
                    } else {
                        currentNamePaint = nameUnknownPaint;
                        nameString = UserController.formatName(user.getFirstName(), user.getLastName());
                    }
                }
            } else {
                nameString = UserController.formatName(user.getFirstName(), user.getLastName());
            }
        }
        if (nameString == null || nameString.length() == 0) {
            nameString = LocaleController.getString(R.string.hidden_name);
        }

        int nameWidth;

        if (!LocaleController.isRTL) {
            nameWidth = getMeasuredWidth() - nameLeft - DisplayController.dp(14) - timeWidth;
        } else {
            nameWidth = getMeasuredWidth() - nameLeft - DisplayController.dp(DisplayController.leftBaseline) - timeWidth;
            nameLeft += timeWidth;
        }
        if (drawNameLock) {
            nameWidth -= DisplayController.dp(4) + lockDrawable.getIntrinsicWidth();
        } else if (drawNameGroup) {
            nameWidth -= DisplayController.dp(4) + groupDrawable.getIntrinsicWidth();
        } else if (drawNameBroadcast) {
            nameWidth -= DisplayController.dp(4) + broadcastDrawable.getIntrinsicWidth();
        }
        if (drawClock) {
            int w = clockDrawable.getIntrinsicWidth() + DisplayController.dp(5);
            nameWidth -= w;
            if (!LocaleController.isRTL) {
                checkDrawLeft = timeLeft - w;
            } else {
                checkDrawLeft = timeLeft + timeWidth + DisplayController.dp(5);
                nameLeft += w;
            }
        } else if (drawCheck2) {
            int w = checkDrawable.getIntrinsicWidth() + DisplayController.dp(5);
            nameWidth -= w;
            if (drawCheck1) {
                nameWidth -= halfCheckDrawable.getIntrinsicWidth() - DisplayController.dp(8);
                if (!LocaleController.isRTL) {
                    halfCheckDrawLeft = timeLeft - w;
                    checkDrawLeft = halfCheckDrawLeft - DisplayController.dp(5.5f);
                } else {
                    checkDrawLeft = timeLeft + timeWidth + DisplayController.dp(5);
                    halfCheckDrawLeft = checkDrawLeft + DisplayController.dp(5.5f);
                    nameLeft += w + halfCheckDrawable.getIntrinsicWidth() - DisplayController.dp(8);
                }
            } else {
                if (!LocaleController.isRTL) {
                    checkDrawLeft = timeLeft - w;
                } else {
                    checkDrawLeft = timeLeft + timeWidth + DisplayController.dp(5);
                    nameLeft += w;
                }
            }
        }

        if (dialogMuted) {
            int w = DisplayController.dp(6) + muteDrawable.getIntrinsicWidth();
            nameWidth -= w;
            if (LocaleController.isRTL) {
                nameLeft += w;
            }
        }

        nameWidth = Math.max(DisplayController.dp(12), nameWidth);
        CharSequence nameStringFinal = TextUtils.ellipsize(nameString.replace("\n", " "), currentNamePaint, nameWidth - DisplayController.dp(12), TextUtils.TruncateAt.END);
        try {
            nameLayout = new StaticLayout(nameStringFinal, currentNamePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        int messageWidth = getMeasuredWidth() - DisplayController.dp(DisplayController.leftBaseline + 16);
        int avatarLeft;
        if (!LocaleController.isRTL) {
            messageLeft = DisplayController.dp(DisplayController.leftBaseline);
            avatarLeft = DisplayController.dp(DisplayController.isTablet ? 13 : 9);
        } else {
            messageLeft = DisplayController.dp(16);
            avatarLeft = getMeasuredWidth() - DisplayController.dp(DisplayController.isTablet ? 65 : 61);
        }
        avatarImage.setImageCoords(avatarLeft, avatarTop, DisplayController.dp(52), DisplayController.dp(52));
        if (drawError) {
            int w = errorDrawable.getIntrinsicWidth() + DisplayController.dp(8);
            messageWidth -= w;
            if (!LocaleController.isRTL) {
                errorLeft = getMeasuredWidth() - errorDrawable.getIntrinsicWidth() - DisplayController.dp(11);
            } else {
                errorLeft = DisplayController.dp(11);
                messageLeft += w;
            }
        } else if (countString != null) {
            countWidth = Math.max(DisplayController.dp(12), (int)Math.ceil(countPaint.measureText(countString)));
            countLayout = new StaticLayout(countString, countPaint, countWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            int w = countWidth + DisplayController.dp(18);
            messageWidth -= w;
            if (!LocaleController.isRTL) {
                countLeft = getMeasuredWidth() - countWidth - DisplayController.dp(19);
            } else {
                countLeft = DisplayController.dp(19);
                messageLeft += w;
            }
            drawCount = true;
        } else {
            drawCount = false;
        }

        if (checkMessage) {
            if (messageString == null) {
                messageString = "";
            }
            String mess = messageString.toString();
            if (mess.length() > 150) {
                mess = mess.substring(0, 150);
            }
            mess = mess.replace("\n", " ");
            messageString = Emoji.replaceEmoji(mess, messagePaint.getFontMetricsInt(), DisplayController.dp(17));
        }
        messageWidth = Math.max(DisplayController.dp(12), messageWidth);
        CharSequence messageStringFinal = TextUtils.ellipsize(messageString, currentMessagePaint, messageWidth - DisplayController.dp(12), TextUtils.TruncateAt.END);
        try {
            messageLayout = new StaticLayout(messageStringFinal, currentMessagePaint, messageWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        double widthpx = 0;
        float left = 0;
        if (LocaleController.isRTL) {
            if (nameLayout != null && nameLayout.getLineCount() > 0) {
                left = nameLayout.getLineLeft(0);
                widthpx = Math.ceil(nameLayout.getLineWidth(0));
                if (dialogMuted) {
                    nameMuteLeft = (int) (nameLeft + (nameWidth - widthpx) - DisplayController.dp(6) - muteDrawable.getIntrinsicWidth());
                }
                if (left == 0) {
                    if (widthpx < nameWidth) {
                        nameLeft += (nameWidth - widthpx);
                    }
                }
            }
            if (messageLayout != null && messageLayout.getLineCount() > 0) {
                left = messageLayout.getLineLeft(0);
                if (left == 0) {
                    widthpx = Math.ceil(messageLayout.getLineWidth(0));
                    if (widthpx < messageWidth) {
                        messageLeft += (messageWidth - widthpx);
                    }
                }
            }
        } else {
            if (nameLayout != null && nameLayout.getLineCount() > 0) {
                left = nameLayout.getLineRight(0);
                if (left == nameWidth) {
                    widthpx = Math.ceil(nameLayout.getLineWidth(0));
                    if (widthpx < nameWidth) {
                        nameLeft -= (nameWidth - widthpx);
                    }
                }
                if (dialogMuted) {
                    nameMuteLeft = (int) (nameLeft + left + DisplayController.dp(6));
                }
            }
            if (messageLayout != null && messageLayout.getLineCount() > 0) {
                left = messageLayout.getLineRight(0);
                if (left == messageWidth) {
                    widthpx = Math.ceil(messageLayout.getLineWidth(0));
                    if (widthpx < messageWidth) {
                        messageLeft -= (messageWidth - widthpx);
                    }
                }
            }
        }
    }

    public void checkCurrentDialogIndex() {
        Dialog dialog = null;
        if (isServerOnly) {
            if (index < MessageController.dialogsServerOnly.size()) {
                dialog = MessageController.dialogsServerOnly.get(index);
            }
        } else {
            if (index < MessageController.dialogs.size()) {
                dialog = MessageController.dialogs.get(index);
            }
        }
        if (dialog != null) {
            if (currentDialogId != dialog.getId() || message != null && message.getId() != dialog.getTopMessage().getId() || unreadCount != dialog.getUnreadCount()) {
                currentDialogId = dialog.getId();
                update(0);
            }
        }
    }

    public void update(int mask) {
        if (isDialogCell) {
            Dialog dialog = MessageController.dialogsMap.get(currentDialogId);
            if (dialog != null && mask == 0) {
                message = dialog.getTopMessage();
                lastUnreadState = message != null && message.isUnread();
                unreadCount = dialog.getUnreadCount();
                lastMessageDate = message != null ? message.getDate() : 0;
                if (message != null) {
                    lastSendState = message.getSendState();
                }
            }
        }

        if (mask != 0) {
            boolean continueUpdate = false;
            if (isDialogCell && (mask & Droid.UPDATE_MASK_USER_PRINT) != 0) {
                CharSequence printString = MessageController.printingStrings.get(currentDialogId);
                if (lastPrintString != null && printString == null || lastPrintString == null && printString != null || lastPrintString != null && printString != null && !lastPrintString.equals(printString)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & Droid.UPDATE_MASK_AVATAR) != 0) {
                if (chat == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & Droid.UPDATE_MASK_NAME) != 0) {
                if (chat == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & Droid.UPDATE_MASK_CHAT_AVATAR) != 0) {
                if (user == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & Droid.UPDATE_MASK_CHAT_NAME) != 0) {
                if (user == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & Droid.UPDATE_MASK_READ_DIALOG_MESSAGE) != 0) {
                if (message != null && lastUnreadState != message.isUnread()) {
                    lastUnreadState = message.isUnread();
                    continueUpdate = true;
                } else if (isDialogCell) {
                    Dialog dialog = MessageController.dialogsMap.get(currentDialogId);
                    if (dialog != null && unreadCount != dialog.getUnreadCount()) {
                        unreadCount = dialog.getUnreadCount();
                        continueUpdate = true;
                    }
                }
            }
            if (!continueUpdate && (mask & Droid.UPDATE_MASK_SEND_STATE) != 0) {
                if (message != null && lastSendState != message.getSendState()) {
                    lastSendState = message.getSendState();
                    continueUpdate = true;
                }
            }

            if (!continueUpdate) {
                return;
            }
        }

        dialogMuted = isDialogCell && MessageController.dialogsMap.get(currentDialogId) != null && MessageController.dialogsMap.get(currentDialogId).isMuted();
        user = null;
        chat = null;

//        int lower_id = (int)currentDialogId;
//        int high_id = (int)(currentDialogId >> 32);
//        if (lower_id != 0) {
//            if (high_id == 1) {
//                chat = UserController.getChat(lower_id);
//            } else {
//                if (lower_id < 0) {
//                    chat = UserController.getChat(-lower_id);
//                } else {
//                    user = UserController.getUser(lower_id);
//                }
//            }
//        } else {
//        }
        if(currentDialogId > 0) {
            user = UserController.getUser((int)currentDialogId);
        } else {
            chat = UserController.getChat((int)currentDialogId);
        }

        TdApi.File photo = null;
        if (user != null) {
            if (user.getPhotoSmall() != null) {
                photo = user.getPhotoSmall();
            }
            avatarDrawable.setInfo(user);
        } else if (chat != null) {
            if (chat.getPhotoSmall() != null) {
                photo = chat.getPhotoSmall();
            }
            avatarDrawable.setInfo(chat);
        }
        avatarImage.setImage(photo, "50_50", avatarDrawable, false);


        if (getMeasuredWidth() != 0 || getMeasuredHeight() != 0) {
            buildLayout();
        } else {
            requestLayout();
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentDialogId == 0) {
            return;
        }

        if (drawNameLock) {
            setDrawableBounds(lockDrawable, nameLockLeft, nameLockTop);
            lockDrawable.draw(canvas);
        } else if (drawNameGroup) {
            setDrawableBounds(groupDrawable, nameLockLeft, nameLockTop);
            groupDrawable.draw(canvas);
        } else if (drawNameBroadcast) {
            setDrawableBounds(broadcastDrawable, nameLockLeft, nameLockTop);
            broadcastDrawable.draw(canvas);
        }

        if (nameLayout != null) {
            canvas.save();
            canvas.translate(nameLeft, DisplayController.dp(13));
            nameLayout.draw(canvas);
            canvas.restore();
        }

        canvas.save();
        canvas.translate(timeLeft, timeTop);
        timeLayout.draw(canvas);
        canvas.restore();

        if (messageLayout != null) {
            canvas.save();
            canvas.translate(messageLeft, messageTop);
            messageLayout.draw(canvas);
            canvas.restore();
        }

        if (drawClock) {
            setDrawableBounds(clockDrawable, checkDrawLeft, checkDrawTop);
            clockDrawable.draw(canvas);
        } else if (drawCheck2) {
            if (drawCheck1) {
                setDrawableBounds(halfCheckDrawable, halfCheckDrawLeft, checkDrawTop);
                halfCheckDrawable.draw(canvas);
                setDrawableBounds(checkDrawable, checkDrawLeft, checkDrawTop);
                checkDrawable.draw(canvas);
            } else {
                setDrawableBounds(checkDrawable, checkDrawLeft, checkDrawTop);
                checkDrawable.draw(canvas);
            }
        }

        if (dialogMuted) {
            setDrawableBounds(muteDrawable, nameMuteLeft, DisplayController.dp(16.5f));
            muteDrawable.draw(canvas);
        }

        if (drawError) {
            setDrawableBounds(errorDrawable, errorLeft, errorTop);
            errorDrawable.draw(canvas);
        } else if (drawCount) {
            setDrawableBounds(countDrawable, countLeft - DisplayController.dp(5.5f), countTop, countWidth + DisplayController.dp(11), countDrawable.getIntrinsicHeight());
            countDrawable.draw(canvas);
            canvas.save();
            canvas.translate(countLeft, countTop + DisplayController.dp(4));
            countLayout.draw(canvas);
            canvas.restore();
        }

        if (useSeparator) {
            if (LocaleController.isRTL) {
                canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth() - DisplayController.dp(DisplayController.leftBaseline), getMeasuredHeight() - 1, linePaint);
            } else {
                canvas.drawLine(DisplayController.dp(DisplayController.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, linePaint);
            }
        }

        avatarImage.draw(canvas);

    }
}
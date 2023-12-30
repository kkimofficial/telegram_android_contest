package ru.nstu.app.model;

import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;

public class Dialog {
    private TdApi.Chat dialog;

    public Dialog(long id) {
        dialog = new TdApi.Chat();
        dialog.id = id;
    }

    public Dialog(TdApi.Chat dialog) {
        this.dialog = dialog;
    }

    public long getId() {
        return dialog.id;
    }

    public Message getTopMessage() {
        return MessageController.getMessage(dialog.topMessage.id);
    }

    public void setTopMessage(Message message) {
        if(message == null) {
            dialog.topMessage = new TdApi.Message();
            return;
        }
        dialog.topMessage = message.getMessage();
    }

    public TdApi.NotificationSettings getNotificationSettings() {
        return dialog.notificationSettings;
    }

    public int getLastReadOutboxMessageId() {
        return dialog.lastReadOutboxMessageId;
    }

    public void setLastReadInboxMessageId(int lastReadInboxMessageId) {
        dialog.lastReadInboxMessageId = lastReadInboxMessageId;
    }

    public int getLastReadInboxMessageId() {
        return dialog.lastReadInboxMessageId;
    }

    public void setLastReadOutboxMessageId(int lastReadOutboxMessageId) {
        dialog.lastReadOutboxMessageId = lastReadOutboxMessageId;
    }

    public int getUnreadCount() {
        return dialog.unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        dialog.unreadCount = unreadCount;
    }

    public void incrementUnreadCount() {
        dialog.unreadCount++;
    }

    public int getChatId() {
        if(dialog.type instanceof TdApi.GroupChatInfo) {
            return ((TdApi.GroupChatInfo)dialog.type).groupChat.id;
        }
        return 0;
    }

    public int getUserId() {
        if(dialog.type instanceof TdApi.PrivateChatInfo) {
            return ((TdApi.PrivateChatInfo)dialog.type).user.id;
        }
        return 0;
    }

    public boolean isMuted() {
        return dialog.notificationSettings != null && dialog.notificationSettings.muteFor > LocaleController.getCurrentTime();
    }

    public Dialog update(TdApi.Chat dialog) {
        this.dialog = dialog;
        return this;
    }
}

package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.MessagesCreator;

public class MuteDialogAction extends Action {
    private long dialogId;
    private boolean mute;

    public MuteDialogAction(long dialogId, boolean mute) {
        this.dialogId = dialogId;
        this.mute = mute;
    }

    @Override
    public void run(Client client) throws Exception {
        TdApi.NotificationSettings notificationSettings = MessageController.getDialog(dialogId).getNotificationSettings();
        notificationSettings.muteFor = mute ? 2147483647 : 0;
        client.send(new TdApi.SetNotificationSettings(new TdApi.NotificationSettingsForChat(dialogId), notificationSettings), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                        if(dialogsCreator != null) {
                            dialogsCreator.notify(Notification.NOTIFICATION_SETTINGS_CHANGED);
                        }

                        MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                        if(messagesCreator != null && MessageController.isMessages()) {
                            messagesCreator.updateActionBar();
                        }
                    }
                });
            }
        });
    }
}

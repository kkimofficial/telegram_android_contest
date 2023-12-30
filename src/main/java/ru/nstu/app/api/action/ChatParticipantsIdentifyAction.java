package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.creator.MessagesCreator;

public class ChatParticipantsIdentifyAction extends Action {
    private int chatId;
    private long dialogId;

    public ChatParticipantsIdentifyAction(int chatId, long dialogId) {
        this.chatId = chatId;
        this.dialogId = dialogId;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetGroupChatFull(chatId), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.GroupChatFull)) {
                    return;
                }

                List<Integer> relations = MessageController.getRelation(chatId);
                relations.clear();
                for(TdApi.ChatParticipant chatParticipant : ((TdApi.GroupChatFull)tlObject).participants) {
                    User user = UserController.getUser(chatParticipant.user.id).update(chatParticipant.user);
                    relations.add(user.getId());
                }

                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                        Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                        if(messagesCreator != null && MessageController.isMessages() && dialogId == dialog.getId()) {
                            messagesCreator.notify(Notification.USER_STATUS);
                        }
                    }
                });
            }
        });
    }
}

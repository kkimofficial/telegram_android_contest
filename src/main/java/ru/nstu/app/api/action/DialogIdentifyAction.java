package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Chat;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.creator.DialogsCreator;

public class DialogIdentifyAction extends Action {
    public long dialogId;

    public DialogIdentifyAction(long dialogId) {
        this.dialogId = dialogId;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetChat(dialogId), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.Chat)) {
                    return;
                }

                TdApi.Chat d = (TdApi.Chat)tlObject;

                final Dialog dialog = MessageController.getDialog(d.id).update(d);

                MessageController.getMessage(d.topMessage.id).update(d.topMessage);

                if(d.type instanceof TdApi.GroupChatInfo) {
                    Chat chat = UserController.getChat(((TdApi.GroupChatInfo) d.type).groupChat.id).update(((TdApi.GroupChatInfo)d.type).groupChat);
                    Droid.doAction(new ChatParticipantsIdentifyAction(chat.getId(), dialog.getId()));
                }

                if(d.type instanceof TdApi.PrivateChatInfo) {
                    UserController.getUser(((TdApi.PrivateChatInfo)d.type).user.id).update(((TdApi.PrivateChatInfo)d.type).user);
                }

                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        if(!MessageController.dialogs.contains(dialog)) {
                            MessageController.dialogs.add(dialog);
                        }
                        DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                        if(dialogsCreator != null) {
                            ((DialogsAdapter) dialogsCreator.getDialogsListView().getAdapter()).sort();
                        }

                    }
                });
            }
        });
    }
}

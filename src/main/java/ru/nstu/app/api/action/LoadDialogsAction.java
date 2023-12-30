package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Chat;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.DialogsMessagesCreator;

public class LoadDialogsAction extends Action {
    @Override
    public void run(final Client client) throws Exception {
        client.send(new TdApi.GetChats(MessageController.dialogs.size(), DialogsAdapter.PAGE_SIZE), new Client.ResultHandler() {
            @Override
            public void onResult(final TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.Chats)) {
                    return;
                }



                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        TdApi.Chats dialogs = (TdApi.Chats)tlObject;

                        for(TdApi.Chat d : dialogs.chats) {

                            Dialog dialog = MessageController.getDialog(d.id).update(d);
                            MessageController.dialogs.add(dialog);
                            MessageController.getMessage(d.topMessage.id).update(d.topMessage);

                            if(d.type instanceof TdApi.GroupChatInfo) {
                                Chat chat = UserController.getChat(((TdApi.GroupChatInfo)d.type).groupChat.id).update(((TdApi.GroupChatInfo)d.type).groupChat);
                                Droid.doAction(new ChatParticipantsIdentifyAction(chat.getId(), dialog.getId()));
                            }

                            if(d.type instanceof TdApi.PrivateChatInfo) {
                                UserController.getUser(((TdApi.PrivateChatInfo)d.type).user.id).update(((TdApi.PrivateChatInfo)d.type).user);
                            }
                        }

                        if(dialogs.chats.length < DialogsAdapter.PAGE_SIZE) {
                            MessageController.dialogsEndReached = true;
                        }

                        MessageController.dialogsServerOnly = MessageController.dialogs;
                        MessageController.loadingDialogs = false;

                        DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                        if(dialogsCreator != null) {
                            ((DialogsAdapter)dialogsCreator.getDialogsListView().getAdapter()).notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }
}

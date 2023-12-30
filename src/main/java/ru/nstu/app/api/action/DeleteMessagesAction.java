package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.MessagesCreator;

public class DeleteMessagesAction extends Action {
    private long dialogId;
    private int[] messagesIds;

    public DeleteMessagesAction(long dialogId, int[] messagesIds) {
        this.dialogId = dialogId;
        this.messagesIds = messagesIds;
    }

    @Override
    public void run(final Client client) throws Exception {
        client.send(new TdApi.DeleteMessages(dialogId, messagesIds), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                System.out.println("==== DELETE MESSAGES RESULT: " + object.getClass());
                final MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                if(messagesCreator != null && MessageController.isMessages()) {
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                            for(Integer id : messagesIds) {
                                Message message = MessageController.getMessage(id);
                                if(dialog != null && dialog.getId() == message.getDialogId()) {
                                    messagesCreator.removeMessage(message);
                                }
                            }
                        }
                    });

                    final DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if(dialogsCreator != null) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                for(Integer id : messagesIds) {
                                    Message message = MessageController.getMessage(id);
                                    final Dialog dialog = MessageController.dialogsMap.get(message.getDialogId());
                                    if(dialog != null && dialog.getId() == message.getDialogId()) {
                                        if(dialog.getTopMessage().getId() == message.getId()) {
                                            client.send(new TdApi.GetChatHistory(dialog.getId(), message.getId(), 0, 1), new Client.ResultHandler() {
                                                @Override
                                                public void onResult(final TdApi.TLObject object) {
                                                    if(object instanceof TdApi.Messages) {
                                                        if(((TdApi.Messages)object).messages.length == 1) {
                                                            Droid.doRunnableUI(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Message message = MessageController.getMessage(((TdApi.Messages)object).messages[0].id).update(((TdApi.Messages)object).messages[0]);
                                                                    dialog.setTopMessage(message);
                                                                    dialogsCreator.notify(Notification.MESSAGE_DELETED);
                                                                }
                                                            });
                                                        } else {
                                                            Droid.doRunnableUI(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ((DialogsAdapter)dialogsCreator.getDialogsListView().getAdapter()).removeItem(dialog);
                                                                    ((DialogsAdapter)dialogsCreator.getDialogsListView().getAdapter()).notifyDataSetChanged();
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }

                            }
                        });
                    }
                }
            }
        });
    }
}

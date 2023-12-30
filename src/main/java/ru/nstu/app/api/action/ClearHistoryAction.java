package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.MessagesCreator;

public class ClearHistoryAction extends Action {
    private long dialogId;
    private boolean remove;

    public ClearHistoryAction(long dialogId, boolean remove) {
        this.dialogId = dialogId;
        this.remove = remove;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.DeleteChatHistory(dialogId), new Client.ResultHandler() {
            @Override
            public void onResult(final TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.Ok)) {
                    return;
                }

                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                        if(dialogsCreator != null) {
                            Dialog dialog = MessageController.getDialog(dialogId);
                            dialog.setTopMessage(null);
                            if(remove) {
                                ((DialogsAdapter)dialogsCreator.getDialogsListView().getAdapter()).removeItem(dialog);
                                ((DialogsAdapter)dialogsCreator.getDialogsListView().getAdapter()).notifyDataSetChanged();
                            } else {
                                dialogsCreator.notify(Notification.MESSAGE_DELETED);
                            }
                        }

                        MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                        if(messagesCreator != null && MessageController.isMessages()) {
                            messagesCreator.removeAllMessages();
                            if(remove) {
                                if(Box.get(Box.DIALOG) == null) {
                                    return;
                                }
                                Box.remove(Box.DIALOG);
                                messagesCreator.showDialogs();
                            }
                        }
                    }
                });
            }
        });
    }
}

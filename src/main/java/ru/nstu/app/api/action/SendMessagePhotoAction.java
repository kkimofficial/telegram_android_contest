package ru.nstu.app.api.action;

import android.graphics.Bitmap;
import android.os.Environment;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.MessagesCreator;

public class SendMessagePhotoAction extends Action {
    private long dialogId;
    private String path;
    private Bitmap bitmap;

    public SendMessagePhotoAction(long dialogId, String path) {
        this.dialogId = dialogId;
        this.path = path;
    }

    public SendMessagePhotoAction(long dialogId, Bitmap bitmap) {
        this.dialogId = dialogId;
        this.bitmap = bitmap;
    }

    @Override
    public void run(Client client) throws Exception {
        if(path == null) {
            File directory = new File(Droid.activity.getExternalFilesDir(null).getAbsolutePath(), "images");
            directory.mkdir();

            path = new File(directory, System.currentTimeMillis() + "").getAbsolutePath();
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.close();
        }

        client.send(new TdApi.SendMessage(dialogId, new TdApi.InputMessagePhoto(path)), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                System.out.println("SEND PHOTO: " + tlObject.getClass());
                if(tlObject instanceof TdApi.Message) {
                    final MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                    final Message message = MessageController.getMessage(((TdApi.Message) tlObject).id).update((TdApi.Message) tlObject);

                    if(messagesCreator != null && MessageController.isMessages()) {
                        final Dialog dialog = (Dialog) Box.get(Box.DIALOG);
                        if(dialog == null || dialog.getId() != message.getDialogId()) {
                            return;
                        }
                        final List<Message> messages = new ArrayList<Message>();
                        messages.add(message);
                        if(messagesCreator.getBottomMessage() != null) {
                            String date1 = LocaleController.formatDate(message.getDate(), LocaleController.DateFormat.MESSAGES_LIST);
                            String date2 = LocaleController.formatDate(messagesCreator.getBottomMessage().getDate(), LocaleController.DateFormat.MESSAGES_LIST);
                            if(!date1.equals(date2)) {
                                Message dateMessage = new Message(null);
                                dateMessage.setContentSystemDate(message.getDate());
                                messages.add(0, dateMessage);
                            }
                        } else {
                            Message dateMessage = new Message(null);
                            dateMessage.setContentSystemDate(message.getDate());
                            messages.add(0, dateMessage);
                        }

                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog == null || dialog.getId() != message.getDialogId()) {
                                    return;
                                }
                                messagesCreator.addMessagesBottom(messages);
                                messagesCreator.scrollToBottom();
                            }
                        });
                    }

                    MessageController.dialogsMap.get(message.getDialogId()).setTopMessage(message);
                    System.out.println("SEND MESSAGE ID:" + ((TdApi.Message)tlObject).id);
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                            if(dialogsCreator != null) {
                                ((DialogsAdapter)dialogsCreator.getDialogsListView().getAdapter()).sort();
                            }
                        }
                    });
                }
            }
        });
    }
}

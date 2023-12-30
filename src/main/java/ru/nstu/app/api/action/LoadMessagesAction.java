package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.nstu.app.android.Box;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.adapter.MessagesAdapter;
import ru.nstu.app.ui.component.panel.SystemNewMessagesPanel;
import ru.nstu.app.ui.creator.DialogsMessagesCreator;
import ru.nstu.app.ui.creator.MessagesCreator;

public class LoadMessagesAction extends Action {
    private long dialogId;
    private int fromId;
    private int offset = 0;
    private int limit;

    private Callback callback;

    public LoadMessagesAction(long dialogId, int fromId, int offset, int limit, Callback callback) {
        this.dialogId = dialogId;
        this.fromId = fromId;
        this.offset = offset;
        this.limit = limit;
        this.callback = callback;
    }

    public LoadMessagesAction(long dialogId, int fromId, int limit, Callback callback) {
        this.dialogId = dialogId;
        this.fromId = fromId;
        this.limit = limit;
        this.callback = callback;
    }

    public LoadMessagesAction(long dialogId, int fromId, int limit) {
        this.dialogId = dialogId;
        this.fromId = fromId;
        this.limit = limit;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetChatHistory(dialogId, fromId, offset, limit), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                System.out.println("=========== Dialog hstory:" + tlObject.getClass());
                if(tlObject instanceof TdApi.Messages) {

                    final List<Message> messages = new ArrayList<Message>(((TdApi.Messages)tlObject).messages.length);
                    for(int i = 0; i < ((TdApi.Messages)tlObject).messages.length; i++) {
                        Message message = MessageController.getMessage(((TdApi.Messages)tlObject).messages[i].id).update(((TdApi.Messages) tlObject).messages[i]);
                        messages.add(0, message);
                    }

                    int receivedCount = messages.size();

                    int lastReadInboxId = (Integer) Box.get(Box.LAST_READ_COUNT);
                    int unreadCount = (Integer) Box.get(Box.UNREAD_COUNT);
                    for(int i = messages.size() - 1; i >= 0; i--) {
                        Message message = null;
                        if(i == 0) {
                            message = new Message(null);
                            message.setContentSystemDate(messages.get(0).getDate());
                        } else if(i > 0) {
                            String date1 = LocaleController.formatDate(messages.get(i).getDate(), LocaleController.DateFormat.MESSAGES_LIST);
                            String date2 = LocaleController.formatDate(messages.get(i - 1).getDate(), LocaleController.DateFormat.MESSAGES_LIST);
                            if(!date1.equals(date2)) {
                                message = new Message(null);
                                message.setContentSystemDate(messages.get(i).getDate());
                            }
                        }
                        if(message != null) {
                            messages.add(i, message);
                        }

                        int j = i;
                        if(message != null) {
                            j++;
                        }
                        if(messages.get(j).getId() == lastReadInboxId) {
                            message = new Message(null);
                            message.setContentSystemDate(-1);

                            int k = j + 1;
                            for(; k < messages.size() && (messages.get(k).isContentSystemDate() || messages.get(k).isOut()); k++);

                            if(k < messages.size()) {
                                if(messages.get(k - 1).isContentSystemDate()) {
                                    k--;
                                }
                                messages.add(k, message);
                            }
                        }
                    }



                    final boolean needMore = messages.size() > 0 && messages.size() < limit && MessageController.isMessages();
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();

                            if(messagesCreator != null && MessageController.isMessages()) {

                                if(messages.size() > 0 && messagesCreator.getTopMessage() != null) {
                                    String date1 = LocaleController.formatDate(messages.get(messages.size() - 1).getDate(), LocaleController.DateFormat.MESSAGES_LIST);
                                    String date2 = LocaleController.formatDate(messagesCreator.getTopMessage().getDate(), LocaleController.DateFormat.MESSAGES_LIST);
                                    if(date1.equals(date2)) {
                                        messagesCreator.removeTopMessage();
                                    }
                                }

                                messagesCreator.addMessagesTop(messages);
                                if(callback != null) {
                                    callback.call(null);
                                }
                                if(!needMore) {
                                    MessageController.loadingMessages = false;
                                }
                            }
                        }
                    });

                    if(needMore) {
                        Droid.doAction(new LoadMessagesAction(dialogId, messages.get(1).getId(), limit - receivedCount));
                    }
                }
            }
        });
    }
}

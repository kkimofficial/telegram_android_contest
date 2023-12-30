package ru.nstu.app.api;

import android.app.NotificationManager;
import android.content.Context;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.api.action.ChatParticipantsIdentifyAction;
import ru.nstu.app.api.action.DialogIdentifyAction;
import ru.nstu.app.api.action.LoadFileAction;
import ru.nstu.app.api.action.MarkReadAction;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.component.adapter.MessagesAdapter;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.DialogsMessagesCreator;
import ru.nstu.app.ui.creator.MessagesCreator;

public class Processor implements Client.ResultHandler {
    @Override
    public void onResult(TdApi.TLObject tlObject) {
        System.out.println("========================== " + tlObject.getClass());
        if(tlObject instanceof TdApi.UpdateFile || tlObject instanceof TdApi.UpdateFileProgress) {
            FileController.onFileUpdate(tlObject);
        }

        if(tlObject instanceof TdApi.UpdateOption) {
            TdApi.UpdateOption updateOption = (TdApi.UpdateOption)tlObject;
            System.out.println("========================== " + tlObject.getClass() + " { " + updateOption.name + " " + updateOption.value + " }");
        }
        if(tlObject instanceof TdApi.UpdateUserLinks) {
            TdApi.UpdateUserLinks updateUserLinks = (TdApi.UpdateUserLinks)tlObject;
            System.out.println("========================== " + tlObject.getClass() + " { " + updateUserLinks.myLink.getClass() + " " + updateUserLinks.foreignLink.getClass() + " }");
        }
        if(tlObject instanceof TdApi.UpdateUserName) {
            TdApi.UpdateUserName updateUserName = (TdApi.UpdateUserName)tlObject;
            User user = UserController.getUser(updateUserName.userId);
            user.setFirstName(updateUserName.firstName);
            user.setLastName(updateUserName.lastName);
            user.setUsername(updateUserName.username);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if(dialogsCreator != null) {
                        dialogsCreator.notify(Notification.USER_NAME);
                    }

                    MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                    if(messagesCreator != null && MessageController.isMessages()) {
                        messagesCreator.notify(Notification.USER_NAME);
                    }
                }
            });

            System.out.println("========================== " + tlObject.getClass() + " { " + updateUserName.firstName + " " + updateUserName.lastName + " }");
        }
        if(tlObject instanceof TdApi.UpdateUserPhoneNumber) {
            TdApi.UpdateUserPhoneNumber updateUserPhone = (TdApi.UpdateUserPhoneNumber)tlObject;
            User user = UserController.getUser(updateUserPhone.userId);
            user.setPhoneNumber(updateUserPhone.phoneNumber);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if(dialogsCreator != null) {
                        dialogsCreator.notify(Notification.USER_PHONE);
                    }

                    MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                    if (messagesCreator != null && MessageController.isMessages()) {
                        messagesCreator.notify(Notification.USER_PHONE);
                    }
                }
            });
            System.out.println("========================== " + tlObject.getClass() + " { " + updateUserPhone.phoneNumber + " }");
        }
        if(tlObject instanceof TdApi.UpdateUserStatus) {
            TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus)tlObject;
            User user = UserController.getUser(updateUserStatus.userId);
            user.setUserStatus(updateUserStatus.status);
        }
        if(tlObject instanceof TdApi.UpdateUserPhoto) {
            TdApi.UpdateUserPhoto updateUserPhoto = (TdApi.UpdateUserPhoto)tlObject;
            User user = UserController.getUser(updateUserPhoto.userId);
            user.setPhotoSmall(updateUserPhoto.photoSmall);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if (dialogsCreator != null) {
                        dialogsCreator.notify(Notification.USER_PHOTO);
                    }

                    MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                    if (messagesCreator != null && MessageController.isMessages()) {
                        messagesCreator.notify(Notification.USER_PHOTO);
                    }
                }
            });
        }

        // ===================== Message updates


        if(tlObject instanceof TdApi.UpdateNewMessage) {
            TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage)tlObject;
            final MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
            final Message message = MessageController.getMessage(updateNewMessage.message.id).update(updateNewMessage.message);
            if(messagesCreator != null && MessageController.isMessages()) {
                final Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                if(dialog == null || dialog.getId() != message.getDialogId()) {
                    if(!MessageController.getDialog(message.getDialogId()).isMuted() && message.getFromId() != UserController.userId) {
                        Droid.notify(message.getNotificationDescription().toString());
                    }
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
                }
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog == null || dialog.getId() != message.getDialogId()) {
                            dialog.incrementUnreadCount();
                            DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                            if(dialogsCreator != null) {
                                dialogsCreator.notify(Notification.INBOX_READ);
                            }
                            if(!MessageController.getDialog(message.getDialogId()).isMuted() && message.getFromId() != UserController.userId) {
                                Droid.notify(message.getNotificationDescription().toString());
                            }
                            return;
                        }
                        Droid.doAction(new MarkReadAction(message));
                        messagesCreator.addMessagesBottom(messages);
                        messagesCreator.scrollToBottom();
                    }
                });
            } else {
                if(!MessageController.getDialog(message.getDialogId()).isMuted() && message.getFromId() != UserController.userId) {
                    Droid.notify(message.getNotificationDescription().toString());
                }
                MessageController.getDialog(message.getDialogId()).incrementUnreadCount();
            }

            final Dialog dialog = MessageController.getDialog(message.getDialogId());
            dialog.setTopMessage(message);
            System.out.println("NEW MESSAGE ID:" + ((TdApi.UpdateNewMessage) tlObject).message.id);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if (dialogsCreator != null) {
                        if (!MessageController.dialogs.contains(dialog)) {
                            Droid.doAction(new DialogIdentifyAction(dialog.getId()));
                        } else {
                            ((DialogsAdapter) dialogsCreator.getDialogsListView().getAdapter()).sort();
                        }

                    }
                }
            });

        }

        if(tlObject instanceof TdApi.UpdateMessageContent) {
            final TdApi.UpdateMessageContent updateMessageContent = (TdApi.UpdateMessageContent)tlObject;
            final Message message = MessageController.getMessage(updateMessageContent.messageId);
            message.setContent(updateMessageContent.newContent);
            final MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
            if(messagesCreator != null && MessageController.isMessages()) {
                final Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog.getId() == updateMessageContent.chatId) {
                            messagesCreator.changeMessage(message);
                        }
                    }
                });
            }
        }

        if(tlObject instanceof TdApi.UpdateMessageId) {
            final TdApi.UpdateMessageId updateMessageId = (TdApi.UpdateMessageId)tlObject;
            MessageController.replace(updateMessageId.oldId, updateMessageId.newId, updateMessageId.chatId);
            final Message message = MessageController.getMessage(updateMessageId.newId);
            final MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
            if(messagesCreator != null && MessageController.isMessages()) {
                final Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog.getId() == message.getDialogId()) {
                            messagesCreator.changeMessage(message);
                        }
                    }
                });
            }
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if (dialogsCreator != null) {
                        dialogsCreator.notify(Notification.MESSAGE_CHANGED);
                    }
                }
            });
        }

        if(tlObject instanceof TdApi.UpdateChatReadInbox) {
            TdApi.UpdateChatReadInbox updateChatReadInbox = (TdApi.UpdateChatReadInbox)tlObject;
            Dialog dialog = MessageController.getDialog(updateChatReadInbox.chatId);
            dialog.setUnreadCount(updateChatReadInbox.unreadCount);
            dialog.setLastReadInboxMessageId(updateChatReadInbox.lastRead);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if (dialogsCreator != null) {
                        dialogsCreator.notify(Notification.INBOX_READ);
                    }
                }
            });
        }

        if(tlObject instanceof TdApi.UpdateChatReadOutbox) {
            TdApi.UpdateChatReadOutbox updateChatReadOutbox = (TdApi.UpdateChatReadOutbox)tlObject;
            final Dialog dialog = MessageController.getDialog(updateChatReadOutbox.chatId);
            dialog.setLastReadOutboxMessageId(updateChatReadOutbox.lastRead);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if (dialogsCreator != null) {
                        dialogsCreator.notify(Notification.OUTBOX_READ);
                    }

                    MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                    if (messagesCreator != null && MessageController.isMessages()) {
                        if (dialog.getId() == ((Dialog) Box.get(Box.DIALOG)).getId()) {
                            messagesCreator.notify(Notification.OUTBOX_READ);
                        }
                    }

                }
            });
        }

        if(tlObject instanceof TdApi.UpdateChatTitle) {
            TdApi.UpdateChatTitle updateChatTitle = (TdApi.UpdateChatTitle)tlObject;
            final Dialog dialog = MessageController.getDialog(updateChatTitle.chatId);
            UserController.getChat(dialog.getChatId()).setTitle(updateChatTitle.title);
            Droid.doRunnableUI(new Runnable() {
                @Override
                public void run() {
                    DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                    if(dialogsCreator != null) {
                        dialogsCreator.notify(Notification.USER_STATUS);
                    }

                    MessagesCreator messagesCreator = Droid.activity.getMessagesCreator();
                    if (messagesCreator != null && MessageController.isMessages()) {
                        if (dialog.getId() == ((Dialog) Box.get(Box.DIALOG)).getId()) {
                            messagesCreator.notify(Notification.USER_STATUS);
                        }
                    }
                }
            });
        }

        if(tlObject instanceof TdApi.UpdateChatParticipantsCount) {
            Dialog dialog = MessageController.getDialog(((TdApi.UpdateChatParticipantsCount)tlObject).chatId);
            Droid.doAction(new ChatParticipantsIdentifyAction(dialog.getChatId(), dialog.getId()));
        }
    }
}

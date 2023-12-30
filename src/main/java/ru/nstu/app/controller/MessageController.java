package ru.nstu.app.controller;

import ru.nstu.app.android.Box;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageController {
    public static List<Dialog> dialogs = new ArrayList<Dialog>();
    public static List<Dialog> dialogsServerOnly = new ArrayList<Dialog>();
    public static boolean dialogsEndReached;
    public static volatile boolean loadingDialogs;

    //===============

    public static Map<Long, String> printingStrings = new HashMap<Long, String>();
    public static Map<Long, Dialog> dialogsMap = new HashMap<Long, Dialog>();
    private static Map<Integer, Message> messagesMap = new ConcurrentHashMap<Integer, Message>();

    public static synchronized Dialog getDialog(long id) {
        Dialog dialog = dialogsMap.get(id);
        if(dialog == null) {
            dialogsMap.put(id, dialog = new Dialog(id));
        }
        return dialog;
    }

    public static volatile boolean loadingMessages;

    private static Map<Integer, List<Integer>> relationsMap = new ConcurrentHashMap<Integer, List<Integer>>();

    public static synchronized List<Integer> getRelation(int chatId) {
        List<Integer> relationList = relationsMap.get(chatId);
        if(relationList == null) {
            MessageController.relationsMap.put(chatId, relationList = new ArrayList<Integer>());
        }
        return relationList;
    }

    public static boolean isMessages() {
        return Box.get(Box.DIALOG) != null;
    }

    public static synchronized Message getMessage(int id) {
        Message message = messagesMap.get(id);
        if(message == null) {
            messagesMap.put(id, message = new Message(id));
        }
        return message;
    }

    public static synchronized void replace(int oldId, int newId, long dialogId) {
        Message message = messagesMap.get(oldId);
        if(message == null) {
            return;
        }
        messagesMap.put(newId, message);
        message.setId(newId);
        if(MessageController.dialogsMap.get(dialogId).getTopMessage().getId() == newId) {
            MessageController.dialogsMap.get(dialogId).setTopMessage(message);
        }
        messagesMap.remove(oldId);
    }

}

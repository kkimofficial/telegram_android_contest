package ru.nstu.app.model;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;

public class Chat {
    private TdApi.GroupChat groupChat;

    public Chat(int id) {
        groupChat = new TdApi.GroupChat();
        groupChat.id = id;
    }

    public Chat(TdApi.GroupChat groupChat) {
        this.groupChat = groupChat;
    }

    public int getId() {
        return groupChat.id;
    }

    public String getTitle() {
        return groupChat.title;
    }

    public void setTitle(String title) {
        groupChat.title = title;
    }

    public TdApi.File getPhotoSmall() {
        return groupChat.photoSmall;
    }

    public int getParticipantsCount() {
        return groupChat.participantsCount;
    }

    public int getOnlineParticipantsCount() {
        int onlineCount = 0;
        List<Integer> relationList = MessageController.getRelation(groupChat.id);
        for(int i = 0; i < relationList.size(); i++) {
            if(UserController.getUser(relationList.get(i)).getUserStatusOnline() != null) {
                onlineCount++;
            }
        }
        return onlineCount;
    }

    public Chat update(TdApi.GroupChat groupChat) {
        this.groupChat = groupChat;
        return this;
    }
}

package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.ui.creator.MessagesCreator;

public class LeaveGroupAction extends Action {
    private long chatId;

    public LeaveGroupAction(long chatId) {
        this.chatId = chatId;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.DeleteChatParticipant(chatId, UserController.userId), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.Ok)) {
                    return;
                }
            }
        });
    }
}

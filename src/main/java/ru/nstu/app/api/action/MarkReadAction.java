package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.model.Message;

public class MarkReadAction extends Action {
    Message message;

    public MarkReadAction(Message message) {
        this.message = message;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetChatHistory(message.getDialogId(), message.getId() + 1, 0, 1), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {

            }
        });
    }
}

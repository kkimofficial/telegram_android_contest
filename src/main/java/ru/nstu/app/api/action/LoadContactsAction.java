package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class LoadContactsAction extends Action {
    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetContacts(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                if(!(tlObject instanceof TdApi.Contacts)) {
                    return;
                }

                List<User> users = new ArrayList<User>();
                for(TdApi.User u : ((TdApi.Contacts)tlObject).users) {
                    User user = UserController.getUser(u.id).update(u);
                    users.add(user);
                }
                UserController.processUsers(users);
            }
        });
    }
}

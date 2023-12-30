package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.creator.DialogsCreator;
import ru.nstu.app.ui.creator.DialogsMessagesCreator;

public class SelfIdentifyAction extends Action {

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.GetMe(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                System.out.println("=== SELF IDENTIFY" + tlObject.getClass());
                if(tlObject instanceof TdApi.User) {
                    UserController.getUser(UserController.userId = ((TdApi.User) tlObject).id).update((TdApi.User)tlObject);
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            DialogsCreator dialogsCreator = Droid.activity.getDialogsCreator();
                            if(dialogsCreator != null) {
                                dialogsCreator.updateNavigationDrawer(Droid.activity);
                            }
                        }
                    });
                }
            }
        });
    }
}

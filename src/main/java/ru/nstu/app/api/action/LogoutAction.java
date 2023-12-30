package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.R;
import ru.nstu.app.android.Droid;

public class LogoutAction extends Action {

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.AuthReset(false), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        Droid.activity.onChangeContentView(R.layout.phone_registration);
                    }
                });
            }
        });
    }
}

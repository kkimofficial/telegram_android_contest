package ru.nstu.app.api.action;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.R;
import ru.nstu.app.android.Droid;

public class SignAction extends Action {
    private String smsCode;

    public SignAction(String smsCode) {
        this.smsCode = smsCode;
    }

    @Override
    public void run(Client client) throws Exception {
        client.send(new TdApi.AuthSetCode(smsCode), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject tlObject) {
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        Droid.activity.onChangeContentView(R.layout.dialogs_messages);
                    }
                });
            }
        });
    }
}

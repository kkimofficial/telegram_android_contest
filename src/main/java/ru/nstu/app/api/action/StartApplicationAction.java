package ru.nstu.app.api.action;

import android.os.Environment;
import android.view.Window;
import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.api.Processor;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;

public class StartApplicationAction extends Action {

    @Override
    public void run(Client client) throws Exception {
        Droid.activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Droid.activity.setContentView(R.layout.main_activity_layout);
        if(!Droid.exists) {
            {
                boolean isSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && Environment.isExternalStorageRemovable();
                if(isSD) {
                    TG.setDir(Droid.activity.getExternalFilesDir(null).getAbsolutePath());
                } else {
                    TG.setDir(Droid.activity.getFilesDir().getAbsolutePath());
                }
            }

            TG.setUpdatesHandler(new Processor());
            Droid.init();
            DisplayController.init();
            LocaleController.init();
        }
        Droid.exists = true;

        if(Droid.activity.currentContentView != 0) {
            Droid.activity.onChangeContentView(Droid.activity.currentContentView);
        } else {
            TG.getClientInstance().send(new TdApi.AuthGetState(), new Client.ResultHandler() {
                @Override
                public void onResult(final TdApi.TLObject tlObject) {
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            if (tlObject instanceof TdApi.AuthStateOk) {
                                Droid.doAction(new LoadContactsAction());
                                MessageController.loadingDialogs = true;
                                Droid.doAction(new LoadDialogsAction());
                                Droid.doAction(new SelfIdentifyAction());
                                Droid.activity.onChangeContentView(R.layout.dialogs_messages);
                            } else {
                                Droid.activity.onChangeContentView(R.layout.phone_registration);
                            }

                        }
                    });
                }
            });
        }

    }
}

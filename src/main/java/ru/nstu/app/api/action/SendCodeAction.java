package ru.nstu.app.api.action;

import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.creator.CodeConfirmationCreator;
import ru.nstu.app.ui.creator.Creator;
import ru.nstu.app.ui.creator.PhoneRegistrationCreator;

public class SendCodeAction extends Action {
    private String number;
    private String code;

    public SendCodeAction(String number, String code) {
        this.number = number;
        this.code = code;
    }

    @Override
    public void run(final Client client) throws Exception {
        if(number != null) {
            client.send(new TdApi.AuthSetPhoneNumber(number), new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.TLObject tlObject) {
//
                    System.out.println("AUTH STATE " + tlObject.getClass());
                    if(tlObject instanceof TdApi.Error) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                PhoneRegistrationCreator phoneRegistrationCreator = Droid.activity.getPhoneRegistrationCreator();
                                if(phoneRegistrationCreator != null) {
                                    phoneRegistrationCreator.notify(Notification.ERROR);
                                }
                            }
                        });
                        return;
                    }

                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            PhoneRegistrationCreator phoneRegistrationCreator = Droid.activity.getPhoneRegistrationCreator();
                            if(phoneRegistrationCreator != null) {
                                phoneRegistrationCreator.notify(Notification.SUCCESS);
                            }
                        }
                    });
                }
            });
        } else if(code != null) {
            client.send(new TdApi.AuthSetCode(code), new Client.ResultHandler() {
                @Override
                public void onResult(TdApi.TLObject tlObject) {
                    if(tlObject instanceof TdApi.Error) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                CodeConfirmationCreator codeConfirmationCreator = Droid.activity.getCodeConfirmationCreator();
                                if(codeConfirmationCreator != null) {
                                    codeConfirmationCreator.notify(Notification.ERROR);
                                }
                            }
                        });
                        return;
                    }
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            CodeConfirmationCreator codeConfirmationCreator = Droid.activity.getCodeConfirmationCreator();
                            if(codeConfirmationCreator != null) {
                                codeConfirmationCreator.notify(Notification.SUCCESS);
                            }
                        }
                    });
                }
            });
        }

    }
}

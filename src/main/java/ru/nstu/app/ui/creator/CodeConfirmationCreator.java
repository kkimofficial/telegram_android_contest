package ru.nstu.app.ui.creator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.nstu.app.R;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.api.action.LoadContactsAction;
import ru.nstu.app.api.action.LoadDialogsAction;
import ru.nstu.app.api.action.SelfIdentifyAction;
import ru.nstu.app.api.action.SendCodeAction;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.MainActivity;
import ru.nstu.app.ui.component.common.MaterialEditText;
import ru.nstu.app.ui.fragment.actionbar.ActionBar;
import ru.nstu.app.ui.fragment.actionbar.ActionBarMenu;

public class CodeConfirmationCreator extends Creator {
    private MaterialEditText codeConfirmationEditText;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private TextView codeConfirmationTextView;

    @Override
    public void create(final MainActivity activity) {
        buildActionBar(activity);
        buildContent(activity);
    }

    private void buildActionBar(final MainActivity activity) {
        ActionBar actionBar = new ActionBar(activity);
        actionBar.setBackgroundColor(0xff54759e);
        actionBar.setTitle(LocaleController.getString(R.string.code_confirmation_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            private boolean pressed;

            @Override
            public void onItemClick(final int id) {
                if(id == -1) {
                    synchronized (this) {
                        if (pressed) {
                            return;
                        }
                        pressed = true;
                    }
                    activity.onChangeContentView(R.layout.phone_registration);
                }
                if (id == 0) {
                    synchronized (this) {
                        if (pressed) {
                            return;
                        }
                        pressed = true;
                    }
                    progressDialog = ProgressDialog.show(Droid.activity, "", LocaleController.getString(R.string.loading), true);
                    Droid.doAction(new SendCodeAction(null, codeConfirmationEditText.getText()));
                    pressed = false;
                }
            }
        });
        actionBar.setBackButtonImage(R.drawable.ic_back);
        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(0, R.drawable.ic_check);
        getRootLayout(activity).addView(actionBar, 0);
    }

    private void buildContent(final MainActivity activity) {


        codeConfirmationEditText = (MaterialEditText)activity.findViewById(R.id.code_confirmation_edit_text);
        codeConfirmationEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeConfirmationEditText.setHint(LocaleController.getString(R.string.code_confirmation_hint));
        codeConfirmationEditText.addTextChangedListener(new TextWatcher() {
            private String beforeText;
            private boolean cleaning;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(cleaning) {
                    return;
                }
                String afterText = s.toString();
                if(afterText.length() > 0 && !afterText.matches("[0-9]+")) {
                    cleaning = true;
                    s.clear();
                    cleaning = false;
                    s.append(beforeText);
                }
                beforeText = s.toString();
            }
        });
    }

    @Override
    public ViewGroup getRootLayout(MainActivity activity) {
        return (ViewGroup)activity.findViewById(R.id.code_confirmation_linear_layout);
    }

    public void notify(Notification notification) {
        if(notification == Notification.ERROR) {
            progressDialog.dismiss();
            codeConfirmationEditText.error();
            codeConfirmationTextView = (TextView)Droid.activity.findViewById(R.id.code_confirmation_text_view);
            codeConfirmationTextView.setVisibility(View.VISIBLE);
        } else {
            progressDialog.dismiss();
            Droid.activity.onChangeContentView(R.layout.dialogs_messages);
            Droid.doAction(new LoadContactsAction());
            Droid.doAction(new LoadDialogsAction());
            Droid.doAction(new SelfIdentifyAction());
        }
    }
}

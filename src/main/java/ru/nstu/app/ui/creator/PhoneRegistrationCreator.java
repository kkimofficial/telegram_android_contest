package ru.nstu.app.ui.creator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.List;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.android.phoneformat.PhoneFormat;
import ru.nstu.app.api.action.SendCodeAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.MainActivity;
import ru.nstu.app.ui.component.adapter.CountriesAdapter;
import ru.nstu.app.ui.component.common.MaterialEditText;
import ru.nstu.app.ui.component.panel.LoadingPanel;
import ru.nstu.app.ui.fragment.actionbar.ActionBar;
import ru.nstu.app.ui.fragment.actionbar.ActionBarMenu;

public class PhoneRegistrationCreator extends Creator {
    private MaterialEditText countryEditText;
    private Button countryButton;
    private MaterialEditText phoneCodeEditText;
    private MaterialEditText phoneNumberEditText;
    private FrameLayout coverLayout;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    private PopupWindow loadingPopupWindow;

    @Override
    public void create(final MainActivity activity) {
        buildActionBar(activity);
        Box.put(Box.COUNTRIES_ADAPTER, new CountriesAdapter(activity));
        buildContent(activity);
    }

    private void buildActionBar(final MainActivity activity) {
        ActionBar actionBar = new ActionBar(activity);
        actionBar.setBackgroundColor(0xff54759e);
        actionBar.setTitle(LocaleController.getString(R.string.register_auth_phone_hint));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            private boolean pressed;

            @Override
            public void onItemClick(final int id) {
                if (id == 0) {
                    synchronized (this) {
                        if (pressed) {
                            return;
                        }
                        pressed = true;
                    }
                    if (phoneCodeEditText.getText().length() > 1 && phoneNumberEditText.getText().length() > 0) {
                        String phoneCode = phoneCodeEditText.getText().substring(1);
                        String phoneNumber = phoneNumberEditText.getText().replaceAll("\\D+", "");
                        Box.put(Box.PHONE_CODE, phoneCode);
                        Box.put(Box.PHONE_NUMBER, phoneNumber);

//                        loadingPopupWindow = new PopupWindow(coverLayout);
//                        loadingPopupWindow.setHeight(DisplayController.screenHeight);
//                        loadingPopupWindow.setWidth(DisplayController.screenWidth);
//                        loadingPopupWindow.showAtLocation(getRootLayout(activity), Gravity.CENTER, 0, 0);

                        progressDialog = ProgressDialog.show(Droid.activity, "", LocaleController.getString(R.string.loading), true);

                        Droid.doAction(new SendCodeAction(phoneCode + phoneNumber, null));

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Droid.activity);
                        builder.setTitle(R.string.incorrect_phone);
                        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.cancel();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.show();
                    }
                    pressed = false;
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(0, R.drawable.ic_check);
        getRootLayout(activity).addView(actionBar, 0);
    }

    private void buildContent(final MainActivity activity) {
        countryButton = (Button)activity.findViewById(R.id.country_button);
        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onChangeContentView(R.layout.country_selection);
            }
        });

        phoneCodeEditText = (MaterialEditText)activity.findViewById(R.id.phone_code_edit_text);
        phoneCodeEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneCodeEditText.setText("+");


        phoneNumberEditText = (MaterialEditText)activity.findViewById(R.id.phone_number_edit_text);
        phoneNumberEditText.setHint(LocaleController.getString(R.string.register_auth_phone_hint));
        phoneNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);

        if(Box.get(Box.PHONE_CODE) != null) {
            phoneCodeEditText.setText("+" + (String)Box.get(Box.PHONE_CODE));
        }
        if(Box.get(Box.COUNTRY) != null) {
            CountriesAdapter.Country country = (CountriesAdapter.Country)Box.get(Box.COUNTRY);
            countryButton.setText(country.name);
            phoneCodeEditText.setText("+" + country.code);
        } else {
            countryButton.setText(LocaleController.getString(R.string.select_country_hint));
        }

        phoneCodeEditText.setSelection(phoneCodeEditText.getText().length());

        phoneCodeEditText.addTextChangedListener(new TextWatcher() {
            private String beforeText = "+";
            private boolean cleaning;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cleaning) {
                    return;
                }
                String afterText = s.toString();
                if (afterText.length() == 0 || afterText.charAt(0) != '+'
                        || afterText.indexOf("+") != afterText.lastIndexOf("+")
                        || (afterText.length() > 1 && !afterText.substring(1).matches("[0-9]+"))) {
                    cleaning = true;
                    s.clear();
                    cleaning = false;
                    s.append(beforeText);
                }
                beforeText = s.toString();
                String phoneCode = phoneCodeEditText.getText().substring(1);
                Box.put(Box.PHONE_CODE, phoneCode);

                CountriesAdapter countriesAdapter = (CountriesAdapter)Box.get(Box.COUNTRIES_ADAPTER);
                if(countriesAdapter.getCountries().containsKey(s.toString().substring(1))) {
                    List<CountriesAdapter.Country> countries = countriesAdapter.getCountries().get(s.toString().substring(1));
                    CountriesAdapter.Country country = countries.get(countries.size() - 1);
                    countryButton.setText(country.name);
                    Box.put(Box.COUNTRY, country);
                } else {
                    Box.remove(Box.COUNTRY);
                    countryButton.setText(LocaleController.getString(R.string.select_country_hint));
                }
            }
        });

        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            private boolean formatting;
            private boolean cleaning;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(formatting) {
                    return;
                }
                if(cleaning) {
                    return;
                }
                String afterText = s.toString();
                cleaning = true;
                s.clear();
                cleaning = false;
                formatting = true;
                String phoneCode = phoneCodeEditText.getText().substring(1);
                afterText = afterText.replaceAll("\\D+", "");
                afterText = PhoneFormat.getInstance().format("+" + phoneCode + afterText).substring(phoneCode.length() + 1);
                s.append(afterText);
                formatting = false;

                String phoneNumber = afterText;
                Box.put(Box.PHONE_NUMBER, phoneNumber);
            }
        });
        if(Box.get(Box.PHONE_NUMBER) != null) {
            phoneNumberEditText.setText((String)Box.get(Box.PHONE_NUMBER));
        }

        coverLayout = new FrameLayout(activity);
        coverLayout.setBackgroundColor(0x88000000);
        LoadingPanel coverLoadingPanel = new LoadingPanel(activity);
        coverLayout.addView(coverLoadingPanel);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)coverLoadingPanel.getLayoutParams();
        layoutParams.width = DisplayController.dp(42);
        layoutParams.height = DisplayController.dp(42);
        layoutParams.gravity = Gravity.CENTER;
        coverLoadingPanel.setLayoutParams(layoutParams);

    }

    @Override
    public ViewGroup getRootLayout(MainActivity activity) {
        return (ViewGroup)activity.findViewById(R.id.phone_registration_linear_layout);
    }

    public void notify(Notification notification) {
        if(notification == Notification.ERROR) {
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(Droid.activity);
            builder.setTitle(R.string.incorrect_phone);
            builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.cancel();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        } else {
            progressDialog.dismiss();
            Droid.activity.onChangeContentView(R.layout.code_confirmation);
        }
    }
}

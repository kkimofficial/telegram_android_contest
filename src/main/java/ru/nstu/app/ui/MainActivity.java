package ru.nstu.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.api.action.SendMessagePhotoAction;
import ru.nstu.app.api.action.StartApplicationAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.ui.creator.*;

public class MainActivity extends Activity {
    private static final String CURRENT_CONTENT_VIEW = "CURRENT_CONTENT_VIEW";
    public int currentContentView;
    private Creator creator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            currentContentView = savedInstanceState.getInt(CURRENT_CONTENT_VIEW, 0);
        }
        Droid.activity = this;
        Droid.doActionDirect(new StartApplicationAction());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_CONTENT_VIEW, currentContentView);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        currentContentView = inState.getInt(CURRENT_CONTENT_VIEW, 0);
    }

    public void onChangeContentView(int contentView) {
        currentContentView = contentView;

        // ========================= PHONE_REGISTRATION =========================

        if(currentContentView == R.layout.phone_registration) {
            View view = getLayoutInflater().inflate(currentContentView, getMainActivityLayout(), false);
            getMainActivityLayout().addView(view, 0);
            (creator = new PhoneRegistrationCreator()).create(this);

            View previousContentView = null;
            try {
                previousContentView = findViewById(R.id.code_confirmation_scroll_view);
            } catch(Exception e) {
                previousContentView = null;
            }

            if(previousContentView == null) {
                try {
                    previousContentView = findViewById(R.id.country_selection_scroll_view);
                } catch (Exception e) {
                    previousContentView = null;
                }
            }

            if(previousContentView == null) {
                try {
                    previousContentView = findViewById(R.id.dialogs_messages_frame_layout);
                } catch (Exception e) {
                    previousContentView = null;
                }
            }

            if(previousContentView != null) {
                Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
                outAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getMainActivityLayout().getChildAt(1).setVisibility(View.GONE);
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                getMainActivityLayout().removeViewAt(1);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                previousContentView.startAnimation(outAnimation);
                findViewById(R.id.phone_registration_scroll_view).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
            }
        }

        // ========================= COUNTRY SELECTION =========================

        if(currentContentView == R.layout.country_selection) {
            View view = getLayoutInflater().inflate(currentContentView, getMainActivityLayout(), false);
            getMainActivityLayout().addView(view);
            (creator = new CountrySelectionCreator()).create(this);

            View previousContentView = null;
            try {
                previousContentView = findViewById(R.id.phone_registration_scroll_view);
            } catch (Exception e) {
                previousContentView = null;
            }
            if(previousContentView != null) {
                Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
                outAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getMainActivityLayout().getChildAt(0).setVisibility(View.GONE);
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                getMainActivityLayout().removeViewAt(0);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                previousContentView.startAnimation(outAnimation);
                findViewById(R.id.country_selection_scroll_view).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            }
        }

        // ========================= CODE_CONFIRMATION =========================

        if(currentContentView == R.layout.code_confirmation) {
            View view = getLayoutInflater().inflate(currentContentView, getMainActivityLayout(), false);
            getMainActivityLayout().addView(view);
            (creator = new CodeConfirmationCreator()).create(this);

            View previousContentView = null;
            try {
                previousContentView = findViewById(R.id.phone_registration_scroll_view);
            } catch (Exception e) {
                previousContentView = null;
            }
            if(previousContentView != null) {
                Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
                outAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getMainActivityLayout().getChildAt(0).setVisibility(View.GONE);
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                getMainActivityLayout().removeViewAt(0);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                previousContentView.startAnimation(outAnimation);
                findViewById(R.id.code_confirmation_scroll_view).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            }

        }

        // ========================= DIALOGS_MESSAGES =========================

        if(currentContentView == R.layout.dialogs_messages) {
            View view = getLayoutInflater().inflate(currentContentView, getMainActivityLayout(), false);
            getMainActivityLayout().addView(view);
            (creator = new DialogsMessagesCreator()).create(this);

            View previousContentView = null;
            try {
                previousContentView = findViewById(R.id.code_confirmation_scroll_view);
            } catch (Exception e) {
                previousContentView = null;
            }
            if(previousContentView != null) {
                Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
                outAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getMainActivityLayout().getChildAt(0).setVisibility(View.GONE);
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                getMainActivityLayout().removeViewAt(0);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                previousContentView.startAnimation(outAnimation);
                findViewById(R.id.dialogs_messages_frame_layout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            }
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Droid.ACTIVITY_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            Droid.doAction(new SendMessagePhotoAction(((Dialog) Box.get(Box.DIALOG)).getId(), (Bitmap) data.getExtras().get("data")));
            MessagesCreator messagesCreator = getMessagesCreator();
            if(messagesCreator != null && MessageController.isMessages()) {
                messagesCreator.notify(Notification.PEEK_SUCCESS);
            }
        }
        if(requestCode == Droid.ACTIVITY_CODE_PEEK_PHOTO && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Droid.doAction(new SendMessagePhotoAction(((Dialog) Box.get(Box.DIALOG)).getId(), BitmapFactory.decodeStream(inputStream)));
                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MessagesCreator messagesCreator = getMessagesCreator();
            if(messagesCreator != null && MessageController.isMessages()) {
                messagesCreator.notify(Notification.PEEK_SUCCESS);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DisplayController.init();
        } else {
            DisplayController.init();
        }
    }

    private ViewGroup getMainActivityLayout() {
        return (ViewGroup)findViewById(R.id.main_activity_layout);
    }

    // =========================  =========================

    public Creator getCreator() {
        return creator;
    }

    public DialogsCreator getDialogsCreator() {
        if(creator instanceof DialogsMessagesCreator) {
            return ((DialogsMessagesCreator)creator).getDialogsCreator();
        }
        return null;
    }

    public MessagesCreator getMessagesCreator() {
        if(creator instanceof DialogsMessagesCreator) {
            return ((DialogsMessagesCreator)creator).getMessagesCreator();
        }
        return null;
    }

    public PhoneRegistrationCreator getPhoneRegistrationCreator() {
        if(creator instanceof PhoneRegistrationCreator) {
            return (PhoneRegistrationCreator)creator;
        }
        return null;
    }

    public CodeConfirmationCreator getCodeConfirmationCreator() {
        if(creator instanceof CodeConfirmationCreator) {
            return (CodeConfirmationCreator)creator;
        }
        return null;
    }
}

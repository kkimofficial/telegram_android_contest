package ru.nstu.app.ui.creator;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.android.phoneformat.PhoneFormat;
import ru.nstu.app.api.action.LoadDialogsAction;
import ru.nstu.app.api.action.LoadMessagesAction;
import ru.nstu.app.api.action.LogoutAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.MainActivity;
import ru.nstu.app.ui.component.adapter.DialogsAdapter;
import ru.nstu.app.ui.component.adapter.MessagesAdapter;
import ru.nstu.app.ui.component.common.AvatarImageView;
import ru.nstu.app.ui.component.panel.DialogPanel;
import ru.nstu.app.ui.fragment.actionbar.ActionBar;

public class DialogsCreator extends Creator {
    private ListView dialogsListView;
    private Drawer.Result drawerResult;
    private TextView headerNameTextView;
    private TextView headerPhoneTextView;
    private AvatarImageView headerImageView;


    @Override
    public void create(final MainActivity activity) {
        buildActionBar(activity);
        buildListView(activity);
        buildNavigationDrawer(activity);
        if(MessageController.isMessages()) {
            showMessages();
        }
    }

    @Override
    public ViewGroup getRootLayout(final MainActivity activity) {
        return (ViewGroup)activity.findViewById(R.id.root_frame_layout);
    }

    private void buildActionBar(final MainActivity activity) {
        ActionBar actionBar = new ActionBar(activity);
        actionBar.setBackgroundColor(0xff54759e);
        actionBar.setTitle(LocaleController.getString(R.string.dialogs_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (id == -1) {
                    drawerResult.openDrawer();
                }
            }
        });
        actionBar.setBackButtonImage(R.drawable.ic_menu);
        getRootLayout(activity).addView(actionBar);

    }

    private void buildListView(final MainActivity activity) {
        dialogsListView = new ListView(activity);
        dialogsListView.setDivider(null);
        dialogsListView.setDividerHeight(0);
        dialogsListView.setAdapter(new DialogsAdapter(activity, false));
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                synchronized (DialogsCreator.class) {
                    if(Box.get(Box.DIALOG) != null) {
                        return;
                    }
                    Dialog dialog = (Dialog) adapterView.getItemAtPosition(i);
                    Box.put(Box.DIALOG, dialog);
                }

                showMessages();
            }
        });
        dialogsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(visibleItemCount > 0) {
                    if(absListView.getLastVisiblePosition() == MessageController.dialogs.size()) {
                        synchronized (DialogsCreator.class) {
                            if(MessageController.loadingDialogs) {
                                return;
                            }
                            MessageController.loadingDialogs = true;
                        }
                        Droid.doAction(new LoadDialogsAction());
                    }
                }
            }
        });
        getRootLayout(activity).addView(dialogsListView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)dialogsListView.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.topMargin = DisplayController.actionBarHeight;
        dialogsListView.setLayoutParams(layoutParams);
    }

    private void buildNavigationDrawer(final MainActivity activity) {
        AccountHeader.Result accountHeaderResult = new AccountHeader() {
            @Override
            public AccountHeader.Result build() {
                AccountHeader.Result accountHeaderResult = super.build();
                (headerNameTextView = mCurrentProfileName).setTextSize(DisplayController.sp(14));
                (headerPhoneTextView = mCurrentProfileEmail).setTextSize(DisplayController.sp(13));

                return accountHeaderResult;
            }
        }
                .withActivity(activity)
                .withHeaderBackground(R.color.system)
                .withSelectionListEnabled(false)
                .addProfiles(new ProfileDrawerItem()
                                .withName("")
                                .withEmail("")
                )
                .withNameTypeface(Typeface.DEFAULT_BOLD)
                .build();

        LinearLayout headerLayout = new LinearLayout(activity);
        headerLayout.setBackgroundColor(activity.getResources().getColor(R.color.system));
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setVerticalGravity(Gravity.BOTTOM);
        DrawerLayout.LayoutParams layoutParams = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT, DisplayController.dp(152));
        headerLayout.setLayoutParams(layoutParams);

        headerImageView = new AvatarImageView(activity);
        headerLayout.addView(headerImageView);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams)headerImageView.getLayoutParams();
        layoutParams1.leftMargin = DisplayController.dp(18);
        layoutParams1.bottomMargin = DisplayController.dp(20);
        layoutParams1.width = DisplayController.dp(62);
        layoutParams1.height = DisplayController.dp(62);
        layoutParams1.gravity = Gravity.BOTTOM;
        headerImageView.setLayoutParams(layoutParams1);

        headerNameTextView = new TextView(activity);
        headerNameTextView.setTextSize(DisplayController.sp(14));
        headerNameTextView.setTextColor(activity.getResources().getColor(R.color.white));
        headerNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
        headerLayout.addView(headerNameTextView);
        layoutParams1 = (LinearLayout.LayoutParams)headerNameTextView.getLayoutParams();
        layoutParams1.leftMargin = DisplayController.dp(20);
        layoutParams1.bottomMargin = 0;
        layoutParams1.gravity = Gravity.BOTTOM;
        headerNameTextView.setLayoutParams(layoutParams1);

        headerPhoneTextView = new TextView(activity);
        headerPhoneTextView.setTextSize(DisplayController.sp(13));
        headerPhoneTextView.setTextColor(0xffd7e8f7);
        headerLayout.addView(headerPhoneTextView);
        layoutParams1 = (LinearLayout.LayoutParams)headerPhoneTextView.getLayoutParams();
        layoutParams1.leftMargin = DisplayController.dp(20);
        layoutParams1.bottomMargin = DisplayController.dp(12);
        layoutParams1.gravity = Gravity.BOTTOM;
        headerPhoneTextView.setLayoutParams(layoutParams1);

        drawerResult = new Drawer()
                .withActivity(activity)
                .withRootView((ViewGroup) activity.findViewById(R.id.navigation_drawer_frame_layout))
//                .withAccountHeader(accountHeaderResult)
                .withHeader(headerLayout)
                .addDrawerItems(new PrimaryDrawerItem()
                                .withName(R.string.logout)
                                .withIcon(R.drawable.ic_logout)
                                .withIdentifier(1)
                                .withTextColor(R.color.black)
                                .withIconColor(R.color.black)
                                .withTypeface(Typeface.DEFAULT_BOLD)
                )
                .withSelectedItem(-1)
                .build();
        if(UserController.userId != 0) {
            updateNavigationDrawer(activity);
        }
        drawerResult.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
            Droid.doAction(new LogoutAction());
            }
        });
    }

    public void updateNavigationDrawer(final MainActivity activity) {
        final User user = UserController.getUser(UserController.userId);
        headerNameTextView.setText(UserController.formatName(user.getFirstName(), user.getLastName()));
        headerPhoneTextView.setText(PhoneFormat.getInstance().format("+" + user.getPhoneNumber()));

        headerImageView.setInfo(user);
        if(FileController.isExists(user.getPhotoSmall())) {
            if(FileController.isCached(user.getPhotoSmall())) {
                FileController.load(headerImageView, FileController.getPath(user.getPhotoSmall()));
            } else {
                FileController.load(FileController.getId(user.getPhotoSmall()), new Callback() {
                    @Override
                    public void call(Object value) {
                        if(value instanceof TdApi.UpdateFile) {
                            FileController.load(headerImageView, FileController.getPath(user.getPhotoSmall()));
                        }
                    }
                });
            }
        }

    }

    public ListView getDialogsListView() {
        return dialogsListView;
    }

    private void showMessages() {
        Dialog dialog = (Dialog)Box.get(Box.DIALOG);
        final ViewSwitcher viewSwitcher = (ViewSwitcher) Droid.activity.findViewById(R.id.root_view_switcher);
        viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(Droid.activity, R.anim.slide_in_right));
        viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(Droid.activity, R.anim.slide_out_right));

        MessageController.loadingMessages = true;
        Box.put(Box.UNREAD_COUNT, dialog.getUnreadCount());
        Box.put(Box.LAST_READ_COUNT, dialog.getLastReadInboxMessageId());
        if(dialog.getUnreadCount() > 0) {
            Droid.doAction(new LoadMessagesAction(dialog.getId(), dialog.getTopMessage().getId() + 1, dialog.getUnreadCount() + 4, new Callback() {
                @Override
                public void call(Object value) {
                    Droid.activity.getMessagesCreator().scrollToTop();
                    viewSwitcher.showNext();
                }
            }));
        } else {
            Droid.doAction(new LoadMessagesAction(dialog.getId(), dialog.getTopMessage().getId() + 1, MessagesAdapter.PAGE_SIZE, new Callback() {
                @Override
                public void call(Object value) {
                    Droid.activity.getMessagesCreator().scrollToBottom();
                    viewSwitcher.showNext();
                }
            }));
        }

        Droid.activity.getMessagesCreator().updateActionBar();
    }

    public void notify(Notification notification) {
        if(notification == Notification.MESSAGE_CHANGED) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.INBOX_READ || notification == Notification.OUTBOX_READ) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.USER_STATUS) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.USER_NAME) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.USER_PHONE) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.USER_PHOTO) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.MESSAGE_DELETED) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
        if(notification == Notification.NOTIFICATION_SETTINGS_CHANGED) {
            int i = dialogsListView.getFirstVisiblePosition();
            int n = dialogsListView.getLastVisiblePosition();
            for(; i < n; i++) {
                View view = dialogsListView.getChildAt(i);
                if(view != null && view instanceof DialogPanel) {
                    ((DialogPanel)view).update(0);
                }
            }
        }
    }
}

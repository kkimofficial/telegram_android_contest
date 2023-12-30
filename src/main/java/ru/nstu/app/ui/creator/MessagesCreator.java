package ru.nstu.app.ui.creator;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.*;

import org.drinkless.td.libcore.telegram.TdApi;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.Notification;
import ru.nstu.app.api.action.ClearHistoryAction;
import ru.nstu.app.api.action.DeleteMessagesAction;
import ru.nstu.app.api.action.LeaveGroupAction;
import ru.nstu.app.api.action.LoadMessagesAction;
import ru.nstu.app.api.action.MuteDialogAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.controller.UserController;
import ru.nstu.app.controller.ViewController;
import ru.nstu.app.model.Chat;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.model.Message;
import ru.nstu.app.model.User;
import ru.nstu.app.ui.MainActivity;
import ru.nstu.app.ui.component.adapter.MessagesAdapter;
import ru.nstu.app.ui.component.common.AvatarImageView;
import ru.nstu.app.ui.component.common.EmojiView;
import ru.nstu.app.ui.component.common.InputView;
import ru.nstu.app.ui.component.panel.MessageContactPanel;
import ru.nstu.app.ui.component.panel.MessagePanel;
import ru.nstu.app.ui.fragment.FixedFrameLayout;
import ru.nstu.app.ui.fragment.actionbar.ActionBar;
import ru.nstu.app.ui.fragment.actionbar.ActionBarMenu;
import ru.nstu.app.ui.fragment.actionbar.ActionBarMenuItem;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.lang.ref.WeakReference;
import java.util.List;

public class MessagesCreator extends Creator {
    private MessagesAdapter messagesAdapter;
    private ActionBar actionBar;
    private AvatarImageView avatarImageView;
    private TextView titleTextView;
    private TextView subtitleTextView;
    private RecyclerView recyclerView;
    private InputView inputView;

    private static final int ACTION_BAR_MENU_ITEM_CLEAR_HISTORY = 1;
    private static final int ACTION_BAR_MENU_ITEM_LEAVE_GROUP = 2;
    private static final int ACTION_BAR_MENU_ITEM_MUTE = 3;

    private static final int ACTION_BAR_ID = Integer.MAX_VALUE - 10;
    private static final int INPUT_VIEW_ID = Integer.MAX_VALUE - 11;

    @Override
    public void create(MainActivity activity) {
        buildActionBar(activity);
        buildInputView(activity);

        buildListView(activity);
    }

    @Override
    public ViewGroup getRootLayout(MainActivity activity) {
        return (ViewGroup)activity.findViewById(R.id.root_frame_layout);
    }

    public void addMessagesTop(List<Message> messages) {
        messagesAdapter.add(messages, 0);
    }

    public void addMessagesBottom(List<Message> messages) {
        messagesAdapter.add(messages, messagesAdapter.getItemCount());
    }

    public void removeTopMessage() {
        messagesAdapter.removeTopMessage();
    }

    public void removeAllMessages() {
        messagesAdapter.clear();
        messagesAdapter.notifyDataSetChanged();
    }

    public Message getTopMessage() {
        return messagesAdapter.get(1);
    }

    public Message getBottomMessage() {
        return messagesAdapter.get(messagesAdapter.getItemCount() - 1);
    }

    public void changeMessage(Message message) {
        int index = messagesAdapter.indexOf(message);
        if(index >= 0) {
            messagesAdapter.notifyItemChanged(index);
        }
    }

    public void removeMessage(Message message) {
        int index = messagesAdapter.indexOf(message);
        if(index >= 0) {
            messagesAdapter.remove(index);
        }
        if(index > 0 && messagesAdapter.get(index - 1).isContentSystemDate()) {
            messagesAdapter.remove(index - 1);
        }
    }

    public void scrollToBottom() {
        if(messagesAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    public void scrollToTop() {
        if(messagesAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(4);
        }
    }

    public Message getPreviousMessage(Message message) {
        int index = messagesAdapter.indexOf(message);
        if(index > 0) {
            return messagesAdapter.get(index - 1);
        }
        return null;
    }

    public Message getNextMessage(Message message) {
        int index = messagesAdapter.indexOf(message);
        if(index >= 0) {
            return messagesAdapter.get(index + 1);
        }
        return null;
    }

    TextView leaveDeleteTextView;
    TextView muteUnmuteTextView;

    public void showDialogs() {
        messagesAdapter.clear();
        DisplayController.hideKeyboard(inputView);
        inputView.hideEmojiPopup(Droid.activity);
        ViewSwitcher viewSwitcher = (ViewSwitcher)Droid.activity.findViewById(R.id.root_view_switcher);
        viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(Droid.activity, R.anim.slide_in_left));
        viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(Droid.activity, R.anim.slide_out_left));
        viewSwitcher.showPrevious();
    }

    private void buildActionBar(final MainActivity activity) {
        actionBar = new ActionBar(activity);
        actionBar.setId(MessagesCreator.ACTION_BAR_ID);
        actionBar.setBackgroundColor(0xff54759e);
        getRootLayout(activity).addView(actionBar);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)actionBar.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        actionBar.setLayoutParams(layoutParams);

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        headerItem.addSubItem(ACTION_BAR_MENU_ITEM_CLEAR_HISTORY, LocaleController.getString(R.string.clear_history), 0);
        leaveDeleteTextView = headerItem.addSubItem(ACTION_BAR_MENU_ITEM_LEAVE_GROUP, LocaleController.getString(R.string.leave_group), 0);
        muteUnmuteTextView = headerItem.addSubItem(ACTION_BAR_MENU_ITEM_MUTE, LocaleController.getString(R.string.mute), 0);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            private boolean pressed;

            @Override
            public void onItemClick(final int id) {
                if (id == -1) {
                    synchronized (MessagesCreator.class) {
                        if(Box.get(Box.DIALOG) == null) {
                            return;
                        }
                        Box.remove(Box.DIALOG);
                    }
                    showDialogs();
                }

                if(id == ACTION_BAR_MENU_ITEM_MUTE) {
                    synchronized (MessagesCreator.class) {
                        if (pressed) {
                            return;
                        }

                        pressed = true;
                    }
                    Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                    Droid.doAction(new MuteDialogAction(dialog.getId(), !dialog.isMuted()));
                    pressed = false;
                }

                if(id == ACTION_BAR_MENU_ITEM_LEAVE_GROUP) {
                    synchronized (MessagesCreator.class) {
                        if(pressed) {
                            return;
                        }
                        pressed = true;
                    }
                    Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                    if(dialog.getChatId() != 0) {
                        Droid.doAction(new LeaveGroupAction(dialog.getId()));
                    } else {
                        Droid.doAction(new ClearHistoryAction(dialog.getId(), true));
                    }
                    pressed = false;
                }

                if(id == ACTION_BAR_MENU_ITEM_CLEAR_HISTORY) {
                    synchronized (MessagesCreator.class) {
                        if(pressed) {
                            return;
                        }
                        pressed = true;
                    }
                    Dialog dialog = (Dialog)Box.get(Box.DIALOG);
                    Droid.doAction(new ClearHistoryAction(dialog.getId(), false));
                    pressed = false;
                }
            }
        });
        actionBar.setBackButtonImage(R.drawable.ic_back);

        FrameLayout avatarContainer = new FrameLayout(activity);
        avatarContainer.setPadding(0, 0, 0, 0);
        actionBar.addView(avatarContainer);
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
        layoutParams2.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams2.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams2.rightMargin = 0;
        layoutParams2.leftMargin = DisplayController.dp(56);
        layoutParams2.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        avatarContainer.setLayoutParams(layoutParams2);

        avatarImageView = new AvatarImageView(activity);
        avatarContainer.addView(avatarImageView);
        layoutParams2 = (FrameLayout.LayoutParams) avatarImageView.getLayoutParams();
        layoutParams2.width = DisplayController.dp(42);
        layoutParams2.height = DisplayController.dp(42);
        layoutParams2.leftMargin = 0;
        layoutParams2.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        avatarImageView.setLayoutParams(layoutParams2);

        titleTextView = new TextView(activity);
        titleTextView.setTextColor(activity.getResources().getColor(R.color.white));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        titleTextView.setLines(1);
        titleTextView.setMaxLines(1);
        titleTextView.setSingleLine(true);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView.setGravity(Gravity.LEFT);
        titleTextView.setCompoundDrawablePadding(DisplayController.dp(4));
        avatarContainer.addView(titleTextView);
        layoutParams2 = (FrameLayout.LayoutParams)titleTextView.getLayoutParams();
        layoutParams2.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams2.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams2.leftMargin = DisplayController.dp(48);
        layoutParams2.topMargin = DisplayController.dp(6);
        layoutParams2.gravity = Gravity.TOP | Gravity.LEFT;
        titleTextView.setLayoutParams(layoutParams2);

        subtitleTextView = new TextView(activity);
        subtitleTextView.setTextColor(0xffd7e8f7);
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        subtitleTextView.setLines(1);
        subtitleTextView.setMaxLines(1);
        subtitleTextView.setSingleLine(true);
        subtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        subtitleTextView.setGravity(Gravity.LEFT);
        avatarContainer.addView(subtitleTextView);
        layoutParams2 = (FrameLayout.LayoutParams)subtitleTextView.getLayoutParams();
        layoutParams2.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams2.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams2.leftMargin = DisplayController.dp(48);
        layoutParams2.topMargin = DisplayController.dp(30);
        layoutParams2.gravity = Gravity.TOP | Gravity.BOTTOM;
        subtitleTextView.setLayoutParams(layoutParams2);
    }

    public void updateActionBar() {
        final Dialog dialog = (Dialog)Box.get(Box.DIALOG);

        if(dialog.getUserId() != 0) {
            final User user = UserController.getUser(dialog.getUserId());
            avatarImageView.setInfo(user);
            if(FileController.isExists(user.getPhotoSmall())) {
                if(FileController.isCached(user.getPhotoSmall())) {
                    FileController.load(avatarImageView, FileController.getPath(user.getPhotoSmall()));
                } else {
                    FileController.load(FileController.getId(user.getPhotoSmall()), new Callback() {
                        @Override
                        public void call(Object value) {
                            if(value instanceof TdApi.UpdateFile) {
                                FileController.load(avatarImageView, FileController.getPath(user.getPhotoSmall()));
                            }
                        }
                    });
                }
            }
            leaveDeleteTextView.setText(LocaleController.getString(R.string.delete_chat));
            titleTextView.setText(UserController.formatName(user.getFirstName(), user.getLastName()));
            subtitleTextView.setText(UserController.formatUserStatus(user));
        } else if(dialog.getChatId() != 0) {
            final Chat chat = UserController.getChat(dialog.getChatId());
            avatarImageView.setInfo(chat);
            if(FileController.isExists(chat.getPhotoSmall())) {
                if(FileController.isCached(chat.getPhotoSmall())) {
                    FileController.load(avatarImageView, FileController.getPath(chat.getPhotoSmall()));
                } else {
                    FileController.load(FileController.getId(chat.getPhotoSmall()), new Callback() {
                        @Override
                        public void call(Object value) {
                            if(value instanceof TdApi.UpdateFile) {
                                FileController.load(avatarImageView, FileController.getPath(chat.getPhotoSmall()));
                            }
                        }
                    });
                }
            }
            leaveDeleteTextView.setText(LocaleController.getString(R.string.leave_group));
            titleTextView.setText(chat.getTitle());
            subtitleTextView.setText(UserController.formatChatStatus(chat));
        }

        muteUnmuteTextView.setText(LocaleController.getString(dialog.isMuted() ? R.string.unmute : R.string.mute));
    }

    private void buildListView(final MainActivity activity) {
        recyclerView = (RecyclerView)LayoutInflater.from(activity).inflate(R.layout.recycler_view, null);//new RecyclerView(activity);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private int visibleCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleCount = Math.max(visibleCount, recyclerView.getChildCount());
                if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() < 5) {
                    Dialog dialog = (Dialog) Box.get(Box.DIALOG);
                    if (messagesAdapter.getItemCount() == 0) {
                        return;
                    }
                    synchronized (MessagesCreator.class) {
                        if (MessageController.loadingMessages) {
                            return;
                        }
                        MessageController.loadingMessages = true;
                    }
                    System.out.println("SCROLL LOAD " + messagesAdapter.get(1).getId());
                    Droid.doAction(new LoadMessagesAction(dialog.getId(), messagesAdapter.get(1).getId(), visibleCount + 5));
                }
            }
        });
        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {

            }
        });
        recyclerView.setItemAnimator(null);
        getRootLayout(activity).addView(recyclerView);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)recyclerView.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.addRule(RelativeLayout.BELOW, actionBar.getId());
        layoutParams.addRule(RelativeLayout.ABOVE, inputView.getId());
        recyclerView.setLayoutParams(layoutParams);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter();
        recyclerView.setAdapter(messagesAdapter);
    }

    private void buildInputView(final MainActivity activity) {
        FixedFrameLayout fixedFrameLayout = null;
        inputView = new InputView(activity) {

        };
        inputView.setId(MessagesCreator.INPUT_VIEW_ID);
        getRootLayout(activity).addView(inputView);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)inputView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        inputView.setLayoutParams(layoutParams);

        final View view = getRootLayout(activity);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean updatedPortrait;
            private boolean updatedLand;

            @Override
            public void onGlobalLayout() {

                int height = view.getRootView().getHeight() - view.getHeight();
                if (height > DisplayController.screenHeight / 3 && height < DisplayController.screenHeight) {
//                    System.out.println("!!! HEIGHT UPDATE: " + (height - DisplayController.statusBarHeight));
                    if(DisplayController.isPortraitOrientation()) {
                        if (updatedPortrait) {
                            return;
                        }
                        updatedPortrait = true;
                        inputView.updateEmojiPopup(height - DisplayController.statusBarHeight);
                    } else {
                        if (updatedLand) {
                            return;
                        }
                        updatedLand = true;
                        inputView.updateEmojiPopup(height - DisplayController.statusBarHeight);
                    }
                }
            }
        });


    }

    private static final int ACTION_BAR_MENU_ITEM_SAVE_TO_GALLERY = 10;
    private static final int ACTION_BAR_MENU_ITEM_DELETE = 11;

    private PopupWindow photoViewPopupWindow;

    public boolean isShowindPhotoViewPopupWindow() {
        return photoViewPopupWindow != null && photoViewPopupWindow.isShowing();
    }

    public void showPhotoView(final MainActivity activity, final Message message) {
        photoViewPopupWindow = new PopupWindow();
        FrameLayout frameLayout = new FrameLayout(activity) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && photoViewPopupWindow != null && photoViewPopupWindow.isShowing()) {
                    photoViewPopupWindow.dismiss();
                }
                return super.dispatchKeyEvent(keyEvent);
            }
        };

        ActionBar actionBar = new ActionBar(activity);
        actionBar.setBackgroundColor(activity.getResources().getColor(R.color.transparent));
        frameLayout.addView(actionBar);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)actionBar.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP;
        actionBar.setLayoutParams(layoutParams);

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_more_photo);
        ViewController.setTouchAnimation(headerItem, android.R.color.transparent, android.R.color.transparent);
        headerItem.addSubItem(ACTION_BAR_MENU_ITEM_SAVE_TO_GALLERY, LocaleController.getString(R.string.save_to_gallery), 0);
        headerItem.addSubItem(ACTION_BAR_MENU_ITEM_DELETE, LocaleController.getString(R.string.delete), 0);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            private boolean pressed;

            @Override
            public void onItemClick(final int id) {
                synchronized (this) {
                    if (pressed) {
                        return;
                    }
                    pressed = true;
                }
                if (id == -1) {
                    photoViewPopupWindow.dismiss();
                    pressed = false;
                } else if (id == ACTION_BAR_MENU_ITEM_SAVE_TO_GALLERY) {

                    pressed = false;
                } else if (id == ACTION_BAR_MENU_ITEM_DELETE) {
                    Droid.doAction(new DeleteMessagesAction(message.getDialogId(), new int[] { message.getId() }));
                    photoViewPopupWindow.dismiss();
                    pressed = false;
                }
            }
        });
        actionBar.setBackButtonImage(R.drawable.ic_back_photo);
        ViewController.setTouchAnimation(actionBar.getBackButtonImageView(), android.R.color.transparent, android.R.color.transparent);

        PhotoView imageView = new PhotoView(activity);
        imageView.setBackgroundColor(0xff000000);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        frameLayout.addView(imageView, 0);
        layoutParams = (FrameLayout.LayoutParams)imageView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        imageView.setLayoutParams(layoutParams);

        TdApi.File file = message.getContentPhotoSizes()[message.getContentPhotoSizes().length - 1].photo;
        if(FileController.isCached(file)) {
//            photoCircularProgressBar.setVisibility(View.GONE);
            FileController.load(imageView, FileController.getPath(file));
        } else {
//            photoCircularProgressBar.setVisibility(View.VISIBLE);
            final WeakReference<ImageView> weakReferenceImageView = new WeakReference<ImageView>(imageView);
//            final WeakReference<CircularProgressBar> weakReferencePhotoCircularProgressBar = new WeakReference<CircularProgressBar>(photoCircularProgressBar);
            final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(file);
            FileController.load(FileController.getId(file), new Callback() {
                @Override
                public void call(final Object value) {
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
//                            if(!MessagePhotoPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
//                                return;
//                            }

                            if (value instanceof TdApi.UpdateFileProgress) {
//                                if(weakReferencePhotoCircularProgressBar.get() != null) {
//                                    weakReferencePhotoCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
//                                }
                            } else if (value instanceof TdApi.UpdateFile) {
//                                if(weakReferencePhotoCircularProgressBar.get() != null) {
//                                    weakReferencePhotoCircularProgressBar.get().finish(null);
//                                }
//                                if(weakReferencePhotoImageView.get() != null && weakReferenceFile.get() != null) {
                                FileController.load(weakReferenceImageView.get(), FileController.getPath(weakReferenceFile.get()), new Callback() {
                                    @Override
                                    public void call(Object value) {
                                        if (weakReferenceImageView.get() != null) {
//                                            new PhotoViewAttacher(weakReferenceImageView.get());
                                        }
                                    }
                                });
//                                }
                            }
                        }
                    });
                }
            });
        }

        photoViewPopupWindow.setContentView(frameLayout);
        photoViewPopupWindow.setFocusable(true);
        photoViewPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_Dialog);
        photoViewPopupWindow.setWidth(DisplayController.screenWidth);
        photoViewPopupWindow.setHeight(DisplayController.screenHeight - DisplayController.statusBarHeight);
        photoViewPopupWindow.showAtLocation(getRootLayout(activity), Gravity.BOTTOM, 0, 0);
    }

    public void notify(Notification notification) {
        if(notification == Notification.USER_STATUS) {
            updateActionBar();
        }
        if(notification == Notification.OUTBOX_READ) {
            int childCount = recyclerView.getChildCount();
            for(int i = 0; i < childCount; i++) {
                View view = recyclerView.getChildAt(i);
                if(view != null) {
                    ((MessagePanel)view).updateBudge();
                }
            }
        }
        if(notification == Notification.USER_NAME) {
            updateActionBar();
            int childCount = recyclerView.getChildCount();
            for(int i = 0; i < childCount; i++) {
                View view = recyclerView.getChildAt(i);
                if(view != null) {
                    ((MessagePanel)view).updateTitle();
                }
            }
        }
        if(notification == Notification.USER_PHONE) {
            updateActionBar();
            int childCount = recyclerView.getChildCount();
            for(int i = 0; i < childCount; i++) {
                View view = recyclerView.getChildAt(i);
                if(view != null) {
                    ((MessagePanel)view).updateTitle();
                    if(view instanceof MessageContactPanel) {
                        ((MessageContactPanel)view).updatePhone();
                    }
                }
            }
        }
        if(notification == Notification.USER_PHOTO) {
            updateActionBar();
            int childCount = recyclerView.getChildCount();
            for(int i = 0; i < childCount; i++) {
                View view = recyclerView.getChildAt(i);
                if(view != null) {
                    ((MessagePanel)view).updateAvatar();
                }
            }
        }
        inputView.processNotification(notification);
    }
}

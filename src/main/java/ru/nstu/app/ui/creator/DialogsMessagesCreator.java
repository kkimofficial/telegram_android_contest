package ru.nstu.app.ui.creator;

import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ViewSwitcher;
import ru.nstu.app.R;
import ru.nstu.app.ui.MainActivity;

public class DialogsMessagesCreator extends Creator {
    private DialogsCreator dialogsCreator;
    private MessagesCreator messagesCreator;

    @Override
    public void create(MainActivity activity) {
        (dialogsCreator = new DialogsCreator() {
            @Override
            public ViewGroup getRootLayout(final MainActivity activity) {
                return (ViewGroup)activity.findViewById(R.id.dialogs_frame_layout);
            }
        }).create(activity);
        (messagesCreator = new MessagesCreator() {
            @Override
            public ViewGroup getRootLayout(final MainActivity activity) {
                return (ViewGroup)activity.findViewById(R.id.messages_relative_layout);
            }
        }).create(activity);
    }

    @Override
    public ViewGroup getRootLayout(MainActivity activity) {
        return (ViewGroup)activity.findViewById(R.id.dialogs_messages_frame_layout);
    }

    public DialogsCreator getDialogsCreator() {
        return dialogsCreator;
    }

    public MessagesCreator getMessagesCreator() {
        return messagesCreator;
    }
}

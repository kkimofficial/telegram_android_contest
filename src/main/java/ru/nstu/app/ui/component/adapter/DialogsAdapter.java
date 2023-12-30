/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.ui.component.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;

import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.MessageController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.ui.component.panel.DialogPanel;
import ru.nstu.app.ui.component.panel.LoadingPanel;

public class DialogsAdapter extends Adapter {
    public static final int PAGE_SIZE = 20;

    private Context mContext;
    private boolean serverOnly;
    private long openedDialogId;
    private int currentCount;

    public DialogsAdapter(Context context, boolean onlyFromServer) {
        mContext = context;
        serverOnly = onlyFromServer;
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getCount();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        int count;
        if (serverOnly) {
            count = MessageController.dialogsServerOnly.size();
        } else {
            count = MessageController.dialogs.size();
        }
        if (count == 0 && MessageController.loadingDialogs) {
            return 0;
        }
        if (!MessageController.dialogsEndReached) {
            count++;
        }
        currentCount = count;
        return count;
    }

    @Override
    public Dialog getItem(int i) {
        if (serverOnly) {
            if (i < 0 || i >= MessageController.dialogsServerOnly.size()) {
                return null;
            }
            return MessageController.dialogsServerOnly.get(i);
        } else {
            if (i < 0 || i >= MessageController.dialogs.size()) {
                return null;
            }
            return MessageController.dialogs.get(i);
        }
    }

    public void removeItem(Dialog dialog) {
        MessageController.dialogs.remove(dialog);
        MessageController.dialogsServerOnly.remove(dialog);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (type == 1) {
            if (view == null) {
                view = new LoadingPanel(mContext);
            }
        } else if (type == 0) {
            if (view == null) {
                view = new DialogPanel(mContext);
            }
            if (view instanceof DialogPanel) { //TODO finally i need to find this crash
                ((DialogPanel) view).useSeparator = (i != getCount() - 1);
                Dialog dialog = null;
                if (serverOnly) {
                    dialog = MessageController.dialogsServerOnly.get(i);
                } else {
                    dialog = MessageController.dialogs.get(i);
                    if (DisplayController.isTablet) {
                        if (dialog.getId() == openedDialogId) {
                            view.setBackgroundColor(0x0f000000);
                        } else {
                            view.setBackgroundColor(0);
                        }
                    }
                }
                ((DialogPanel) view).setDialog(dialog, i, serverOnly);
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (serverOnly && i == MessageController.dialogsServerOnly.size() || !serverOnly && i == MessageController.dialogs.size()) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        int count;
        if (serverOnly) {
            count = MessageController.dialogsServerOnly.size();
        } else {
            count = MessageController.dialogs.size();
        }
        if (count == 0 && MessageController.loadingDialogs) {
            return true;
        }
        if (!MessageController.dialogsEndReached) {
            count++;
        }
        return count == 0;
    }

    public void sort() {
        Collections.sort(MessageController.dialogsServerOnly, new Comparator<Dialog>() {
            @Override
            public int compare(Dialog lhs, Dialog rhs) {
                return rhs.getTopMessage().getId() - lhs.getTopMessage().getId();
            }
        });
//        Collections.sort(MessageController.dialogs, new Comparator<Dialog>() {
//            @Override
//            public int compare(Dialog lhs, Dialog rhs) {
//                return lhs.getTopMessage().getId() - lhs.getTopMessage().getId();
//            }
//        });
        notifyDataSetChanged();
    }
}
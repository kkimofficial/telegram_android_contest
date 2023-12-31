/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package ru.nstu.app.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.nstu.app.R;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.ui.fragment.actionbar.ActionBar;
import ru.nstu.app.ui.fragment.actionbar.ActionBarLayout;

public class BaseFragment {
    private boolean isFinished = false;
    protected AlertDialog visibleDialog = null;

    public View fragmentView;
    public ActionBarLayout parentLayout;
    public ActionBar actionBar;
    protected int classGuid = 0;
    protected Bundle arguments;
    public boolean swipeBackEnabled = true;
    public boolean hasOwnBackground = false;

    public BaseFragment() {
        //classGuid = ConnectionsManager.getInstance().generateClassGuid();
    }

    public BaseFragment(Bundle args) {
        arguments = args;
        //classGuid = ConnectionsManager.getInstance().generateClassGuid();
    }

    public View createView(Context context, LayoutInflater inflater) {
        return null;
    }

    public Bundle getArguments() {
        return arguments;
    }

    public void setParentLayout(ActionBarLayout layout) {
        if (parentLayout != layout) {
            parentLayout = layout;
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup) fragmentView.getParent();
                if (parent != null) {
                    try {
                        parent.removeView(fragmentView);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
                fragmentView = null;
            }
            if (actionBar != null) {
                ViewGroup parent = (ViewGroup) actionBar.getParent();
                if (parent != null) {
                    try {
                        parent.removeView(actionBar);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }
            if (parentLayout != null) {
                actionBar = new ActionBar(parentLayout.getContext());
                actionBar.parentFragment = this;
                actionBar.setBackgroundColor(0xff54759e);
                //actionBar.setItemsBackground(R.drawable.bar_selector);
            }
        }
    }

    public void finishFragment() {
        finishFragment(true);
    }

    public void finishFragment(boolean animated) {
        if (isFinished || parentLayout == null) {
            return;
        }
        parentLayout.closeLastFragment(animated);
    }

    public void removeSelfFromStack() {
        if (isFinished || parentLayout == null) {
            return;
        }
        parentLayout.removeFragmentFromStack(this);
    }

    public boolean onFragmentCreate() {
        return true;
    }

    public void onFragmentDestroy() {
        //ConnectionsManager.getInstance().cancelRpcsForClassGuid(classGuid);
        isFinished = true;
        if (actionBar != null) {
            actionBar.setEnabled(false);
        }
    }

    public void onResume() {

    }

    public void onPause() {
        if (actionBar != null) {
            actionBar.onPause();
        }
        try {
            if (visibleDialog != null && visibleDialog.isShowing()) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {

    }

    public boolean onBackPressed() {
        return true;
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {

    }

    public void saveSelfArgs(Bundle args) {

    }

    public void restoreSelfArgs(Bundle args) {

    }

    public boolean presentFragment(BaseFragment fragment) {
        return parentLayout != null && parentLayout.presentFragment(fragment);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast) {
        return parentLayout != null && parentLayout.presentFragment(fragment, removeLast);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation) {
        return parentLayout != null && parentLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
    }

    public Activity getParentActivity() {
        if (parentLayout != null) {
            return parentLayout.parentActivity;
        }
        return null;
    }

    public void startActivityForResult(final Intent intent, final int requestCode) {
        if (parentLayout != null) {
            parentLayout.startActivityForResult(intent, requestCode);
        }
    }

    public void onBeginSlide() {
        try {
            if (visibleDialog != null && visibleDialog.isShowing()) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        if (actionBar != null) {
            actionBar.onPause();
        }
    }

    public void onOpenAnimationEnd() {

    }

    public void onLowMemory() {

    }

    public boolean needAddActionBar() {
        return true;
    }

    public AlertDialog showAlertDialog(AlertDialog.Builder builder) {
        if (parentLayout == null || parentLayout.checkTransitionAnimation() || parentLayout.animationInProgress || parentLayout.startedTracking) {
            return null;
        }
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            visibleDialog = builder.show();
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                    onDialogDismiss();
                }
            });
            return visibleDialog;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return null;
    }

    protected void onDialogDismiss() {

    }
}
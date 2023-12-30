package ru.nstu.app.controller;

import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import ru.nstu.app.R;
import ru.nstu.app.android.FileLog;

public class ViewController {
    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect)mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return 0;
    }

    public static void clearCursorDrawable(EditText editText) {
        if (editText == null || Build.VERSION.SDK_INT < 12) {
            return;
        }
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.setInt(editText, 0);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    // ==============================================

    public static void setTouchAnimation(final View view, final int colorId, final int defaultColorId) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setBackgroundColor(view.getContext().getResources().getColor(colorId));
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    view.setBackgroundColor(view.getResources().getColor(defaultColorId));
                }
                if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    if(!rect.contains(v.getLeft() + (int)event.getX(), v.getTop() + (int)event.getY())) {
                        view.setBackgroundColor(view.getResources().getColor(defaultColorId));
                    }
                }
                return false;
            }
        });

    }
}

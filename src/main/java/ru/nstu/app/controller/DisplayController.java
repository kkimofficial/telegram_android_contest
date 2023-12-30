package ru.nstu.app.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.text.style.TypefaceSpan;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.ui.MainActivity;

public class DisplayController {
    public static int screenWidth;
    public static int screenHeight;
    public static float density;
    public static boolean isTablet;
    public static int leftBaseline;
    public static int statusBarHeight;
    public static int actionBarHeight;
    public static boolean isCustomTheme;
    public static boolean isSmallTablet;
    public static int minTabletSide;
    public static int fontSize;
    public static int photoSize;

    public static void init() {
        {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Droid.activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
            density = displayMetrics.density;
        }
        isTablet = false;
        leftBaseline = isTablet ? 80 : 72;
        {
            statusBarHeight = 0;
            int resourceId = Droid.activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if(resourceId > 0) {
                statusBarHeight = Droid.activity.getResources().getDimensionPixelSize(resourceId);
            }
        }
        {
            if(isTablet) {
                actionBarHeight = dp(64);
            } else {
                if(Droid.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    actionBarHeight = dp(48);
                } else {
                    actionBarHeight = dp(56);
                }
            }
        }
        isCustomTheme = false;
        {
            float minSide = Math.min(screenWidth, screenHeight) / density;
            isSmallTablet = minSide <= 700;
        }
        {
            if (!isSmallTablet) {
                int smallSide = Math.min(screenWidth, screenHeight);
                int leftSide = smallSide * 35 / 100;
                if (leftSide < dp(320)) {
                    leftSide = dp(320);
                }
                minTabletSide = smallSide - leftSide;
            } else {
                int smallSide = Math.min(screenWidth, screenHeight);
                int maxSide = Math.max(screenWidth, screenHeight);
                int leftSide = maxSide * 35 / 100;
                if (leftSide < dp(320)) {
                    leftSide = dp(320);
                }
                minTabletSide = Math.min(smallSide, maxSide - leftSide);
            }
        }
        fontSize = isTablet ? 18 : 16;
        photoSize = Build.VERSION.SDK_INT >= 16 ? 1280 : 800;
    }

    public static Typeface typeface() {
        return Typeface.createFromAsset(Droid.activity.getAssets(), "fonts/rmedium.ttf");
    }

    public static TypefaceSpan typefaceSpan() {
        return new TypefaceSpan("fonts/rmedium.ttf");
    }

    public static int dp(float value) {
        return (int)Math.ceil(density * value);
    }

    public static int sp(float value) {
        return dp(value);
    }

    public static float pixels(float cm, boolean isX) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Droid.activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return (cm / 2.54f) * (isX ? displayMetrics.xdpi : displayMetrics.ydpi);
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static int prevOrientation = -10;

    public static void lockOrientation(Activity activity) {
        if (activity == null || prevOrientation != -10 || Build.VERSION.SDK_INT < 9) {
            return;
        }
        try {
            prevOrientation = activity.getRequestedOrientation();
            WindowManager manager = (WindowManager)Droid.getSystemService(Activity.WINDOW_SERVICE);
            if (manager != null && manager.getDefaultDisplay() != null) {
                int rotation = manager.getDefaultDisplay().getRotation();
                int orientation = activity.getResources().getConfiguration().orientation;
                int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
                int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;
                if (Build.VERSION.SDK_INT < 9) {
                    SCREEN_ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    SCREEN_ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                }

                if (rotation == Surface.ROTATION_270) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                } else if (rotation == Surface.ROTATION_90) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                } else if (rotation == Surface.ROTATION_0) {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                } else {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public static void unlockOrientation(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < 9) {
            return;
        }
        try {
            if (prevOrientation != -10) {
                activity.setRequestedOrientation(prevOrientation);
                prevOrientation = -10;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public static boolean isPortraitOrientation() {
        return Droid.activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}

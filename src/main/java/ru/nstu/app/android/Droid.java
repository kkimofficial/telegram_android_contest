package ru.nstu.app.android;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import org.drinkless.td.libcore.telegram.TG;

import ru.nstu.app.R;
import ru.nstu.app.api.action.Action;
import ru.nstu.app.ui.MainActivity;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Droid {
    public static MainActivity activity;
    public static volatile boolean exists;

    // ========================= API CALL =========================

    public static void init() {
        actionThread = new Thread() {
            @Override
            public void run() {
                for(; !isInterrupted(); ) {
                    Action action = null;
                    try {
                        (action = actionQueue.take()).run(TG.getClientInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        actionThread.start();
        uiHandler = new Handler(Looper.getMainLooper());
    }

    private static BlockingQueue<Action> actionQueue = new LinkedBlockingQueue<Action>();
    private static Thread actionThread;

    public static void doAction(Action action) {
        try {
            actionQueue.put(action);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void doActionDirect(Action action) {
        try {
            action.run(TG.getClientInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Handler uiHandler;

    public static void doRunnableUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    public static void cancelRunnableUI(Runnable runnable) {
        uiHandler.removeCallbacks(runnable);

    }

    public static void doRunnableUI(Runnable runnable, long delay) {
        uiHandler.postDelayed(runnable, delay);
    }

    public static Object getSystemService(String name) {
        return activity.getSystemService(name);
    }

    // ========================= SHARED PREFERENCES =========================

    public static final String PREFERENCES_COMMON = "VOICE_COMMON";
    public static final String PREFERENCES_EMOJI = "VOICE_EMOJI";

    public static final String PREFERENCES_COMMON_KEYBOARD_HEIGHT_PORTRAIT = "PREFERENCES_COMMON_KEYBOARD_HEIGHT_PORTRAIT";
    public static final String PREFERENCES_COMMON_KEYBOARD_HEIGHT_LAND = "PREFERENCES_COMMON_KEYBOARD_HEIGHT_LAND";

    public static final String PREFERENCES_EMOJI_RECENTS = "VOICE_EMOJI_RECENTS";

    public static void save(String name, String key, Object value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        if(value instanceof String) {
            editor.putString(key, (String)value);
        } else if(value instanceof Integer) {
            editor.putInt(key, (Integer)value);
        }
        editor.commit();
    }

    public static Object load(String name, String key, Object defaultValue) {
        SharedPreferences preferences = activity.getSharedPreferences(name, Context.MODE_PRIVATE);
        if(defaultValue instanceof Integer) {
            return preferences.getInt(key, (Integer)defaultValue);
        } else if(defaultValue instanceof String) {
            return preferences.getString(key, (String)defaultValue);
        }
        return null;
    }

    // ========================= UTILS =========================

    public boolean isInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static String md5(String value) {
        return value;
    }

    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        OutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = sourceFile.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
        return true;
    }

    public static final int UPDATE_MASK_NAME = 1;
    public static final int UPDATE_MASK_AVATAR = 2;
    public static final int UPDATE_MASK_STATUS = 4;
    public static final int UPDATE_MASK_CHAT_AVATAR = 8;
    public static final int UPDATE_MASK_CHAT_NAME = 16;
    public static final int UPDATE_MASK_CHAT_MEMBERS = 32;
    public static final int UPDATE_MASK_USER_PRINT = 64;
    public static final int UPDATE_MASK_USER_PHONE = 128;
    public static final int UPDATE_MASK_READ_DIALOG_MESSAGE = 256;
    public static final int UPDATE_MASK_SELECT_DIALOG = 512;
    public static final int UPDATE_MASK_PHONE = 1024;
    public static final int UPDATE_MASK_NEW_MESSAGE = 2048;
    public static final int UPDATE_MASK_SEND_STATE = 4096;
    public static final int UPDATE_MASK_ALL = UPDATE_MASK_AVATAR | UPDATE_MASK_STATUS | UPDATE_MASK_NAME | UPDATE_MASK_CHAT_AVATAR | UPDATE_MASK_CHAT_NAME | UPDATE_MASK_CHAT_MEMBERS | UPDATE_MASK_USER_PRINT | UPDATE_MASK_USER_PHONE | UPDATE_MASK_READ_DIALOG_MESSAGE | UPDATE_MASK_PHONE;

    public static final int ACTIVITY_CODE_TAKE_PHOTO = 1;
    public static final int ACTIVITY_CODE_PEEK_PHOTO = 2;

    private static volatile int sequence;
    public static void notify(String text) {
        NotificationManager notificationManager = (NotificationManager)Droid.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(sequence++, new android.app.Notification.Builder(Droid.activity).setSmallIcon(R.drawable.ic_badge_blue).setTicker(text).setContentTitle("Voice").setContentText(text).setDefaults(android.app.Notification.DEFAULT_ALL).getNotification());
    }
}

package ru.nstu.app.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.telegram.android.MediaController;
import org.telegram.messenger.Utilities;

import java.nio.ByteBuffer;

public class NativeProvider {
    static {
        try {
            System.loadLibrary("tmessages.7");
        } catch (UnsatisfiedLinkError e) {
            Log.w("DLTD", "Can't find tmessages.7", e);
        }
    }

    public static void loadBitmap(String path, Bitmap bitmap, int scale, int width, int height, int stride) {
        Utilities.loadBitmap(path, bitmap, scale, width, height, stride);
    }

    public static Bitmap loadWebpImage(ByteBuffer buffer, int len, BitmapFactory.Options options) {
        return Utilities.loadWebpImage(buffer, len, options);
    }

    public static int openOpusFile(String path) {
        return MediaController.wOpenOpusFile(path);
    }

    public static void readOpusFile(ByteBuffer buffer, int capacity, int[] args) {
        MediaController.wReadOpusFile(buffer, capacity, args);
    }

    public static int seekOpusFile(float progress) {
        return MediaController.wSeekOpusFile(progress);
    }

    public static int isOpusFile(String path) {
        return MediaController.wIsOpusFile(path);
    }

    public static long getTotalPcmDuration() {
        return MediaController.wGetTotalPcmDuration();
    }
}

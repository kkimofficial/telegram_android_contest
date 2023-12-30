package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;

public class Utilities {
    public native static long doPQNative(long _what);
    public native static void loadBitmap(String path, Bitmap bitmap, int scale, int width, int height, int stride);
    public native static int pinBitmap(Bitmap bitmap);
    public native static void blurBitmap(Object bitmap, int radius);
    public native static void calcCDT(ByteBuffer hsvBuffer, int width, int height, ByteBuffer buffer);
    public native static Bitmap loadWebpImage(ByteBuffer buffer, int len, BitmapFactory.Options options);
    public native static Bitmap loadBpgImage(ByteBuffer buffer, int len, BitmapFactory.Options options);
    public native static int convertVideoFrame(ByteBuffer src, ByteBuffer dest, int destFormat, int width, int height, int padding, int swap);
    private native static void aesIgeEncryption(ByteBuffer buffer, byte[] key, byte[] iv, boolean encrypt, int offset, int length);

}

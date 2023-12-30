package org.telegram.android;

import java.nio.ByteBuffer;

public class MediaController {
    private native int startRecord(String path);
    private native int writeFrame(ByteBuffer frame, int len);
    private native void stopRecord();
    private native int openOpusFile(String path);
    private native int seekOpusFile(float position);
    private native int isOpusFile(String path);
    private native void closeOpusFile();
    private native void readOpusFile(ByteBuffer buffer, int capacity, int[] args);
    private native long getTotalPcmDuration();

    public static int wOpenOpusFile(String path) {
        return new MediaController().openOpusFile(path);
    }

    public static void wReadOpusFile(ByteBuffer buffer, int capacity, int[] args) {
        new MediaController().readOpusFile(buffer, capacity, args);
    }

    public static int wSeekOpusFile(float position) {
        return new MediaController().seekOpusFile(position);
    }

    public static int wIsOpusFile(String path) {
        return new MediaController().isOpusFile(path);
    }

    public static long wGetTotalPcmDuration() {
        return new MediaController().getTotalPcmDuration();
    }
}

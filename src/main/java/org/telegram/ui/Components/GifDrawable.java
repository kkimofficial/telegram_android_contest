package org.telegram.ui.Components;

public class GifDrawable {
    private static native void renderFrame(int[] pixels, int gifFileInPtr, int[] metaData);
    private static native int openFile(int[] metaData, String filePath);
    private static native void free(int gifFileInPtr);
    private static native void reset(int gifFileInPtr);
    private static native void setSpeedFactor(int gifFileInPtr, float factor);
    private static native String getComment(int gifFileInPtr);
    private static native int getLoopCount(int gifFileInPtr);
    private static native int getDuration(int gifFileInPtr);
    private static native int getCurrentPosition(int gifFileInPtr);
    private static native int seekToTime(int gifFileInPtr, int pos, int[] pixels);
    private static native int seekToFrame(int gifFileInPtr, int frameNr, int[] pixels);
    private static native int saveRemainder(int gifFileInPtr);
    private static native int restoreRemainder(int gifFileInPtr);
    private static native long getAllocationByteCount(int gifFileInPtr);
}

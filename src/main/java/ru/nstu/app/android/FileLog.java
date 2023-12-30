package ru.nstu.app.android;

public class FileLog {
    public static void e(String tag, Exception e) {
        e.printStackTrace();
    }

    public static void e(String tag, String message) {

    }

    public static void e(String tag, String message, Throwable t) {
        t.printStackTrace();
    }

    public static void e(String tag, Throwable t) {
        t.printStackTrace();
    }
}

package ru.nstu.app.android;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Box {
    private static Map<String, Object> values = new ConcurrentHashMap<String, Object>();

    public static Object get(String key) {
        return values.get(key);
    }

    public static Object put(String key, Object value) {
        values.put(key, value);
        return value;
    }

    public static void remove(String key) {
        values.remove(key);
    }

    // ========================= KEYS =========================

    public static final String DIALOG = "DIALOG";
    public static final String PHONE_CODE = "PHONE_CODE";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String COUNTRY = "COUNTRY";
    public static final String COUNTRIES_ADAPTER = "COUNTRIES_ADAPTER";
    public static final String UNREAD_COUNT = "UNREAD_COUNT";
    public static final String LAST_READ_COUNT = "LAST_READ_COUNT";
}

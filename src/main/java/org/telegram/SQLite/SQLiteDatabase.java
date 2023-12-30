package org.telegram.SQLite;

import android.database.sqlite.SQLiteException;

public class SQLiteDatabase {
    native int opendb(String fileName, String tempDir) throws SQLiteException;
    native void closedb(int sqliteHandle) throws SQLiteException;
    native void beginTransaction(int sqliteHandle);
    native void commitTransaction(int sqliteHandle);
}

package org.telegram.SQLite;

import android.database.sqlite.SQLiteException;

import java.nio.ByteBuffer;

public class SQLitePreparedStatement {
    private int queryArgsCount;

    native void bindByteBuffer(int statementHandle, int index, ByteBuffer value, int length) throws SQLiteException;
    native void bindString(int statementHandle, int index, String value) throws SQLiteException;
    native void bindInt(int statementHandle, int index, int value) throws SQLiteException;
    native void bindLong(int statementHandle, int index, long value) throws SQLiteException;
    native void bindDouble(int statementHandle, int index, double value) throws SQLiteException;
    native void bindNull(int statementHandle, int index) throws SQLiteException;
    native void reset(int statementHandle) throws SQLiteException;
    native int prepare(int sqliteHandle, String sql) throws SQLiteException;
    native void finalize(int statementHandle) throws SQLiteException;
    native int step(int statementHandle) throws SQLiteException;
}

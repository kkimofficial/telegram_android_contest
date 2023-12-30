package org.telegram.SQLite;

import java.nio.ByteBuffer;

public class SQLiteCursor {
    native int columnType(int statementHandle, int columnIndex);
    native int columnIsNull(int statementHandle, int columnIndex);
    native int columnIntValue(int statementHandle, int columnIndex);
    native long columnLongValue(int statementHandle, int columnIndex);
    native double columnDoubleValue(int statementHandle, int columnIndex);
    native String columnStringValue(int statementHandle, int columnIndex);
    native byte[] columnByteArrayValue(int statementHandle, int columnIndex);
    native int columnByteArrayLength(int statementHandle, int columnIndex);
    native int columnByteBufferValue(int statementHandle, int columnIndex, ByteBuffer buffer);
}

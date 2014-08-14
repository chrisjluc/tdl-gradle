package com.ac.tdl.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper implements DbContract {
    private static final String SQL_CREATE = "SQL CREATE";
    private static final String SQL_DELETE = "SQL DELETE";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "tdl.db";
    private static final String VARCHAR = " VARCHAR";
    private static final String COMMA_SEP = ",";
    private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY,";
    private static final String INT = " INT";
    private static final String LONG = " LONG";
    private static final String TINY_INT = " TINY INT";
    private static final String TIMESTAMP = " TIMESTAMP";

    private static DbHelper instance;

    public static synchronized DbHelper setInstance(Context context) {
        if (instance == null)
            instance = new DbHelper(context);
        return instance;
    }

    public static synchronized DbHelper getInstance() {
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String HASHTAG_TABLE_CREATE = "CREATE TABLE " + HashtagTable.TABLE_NAME
                + " (" + HashtagTable.COLUMN_NAME_ID + PRIMARY_KEY
                + HashtagTable.COLUMN_NAME_HASHTAG_LABEL + varchar(48)
                + COMMA_SEP + HashtagTable.COLUMN_NAME_DATE_CREATED + TIMESTAMP
                + COMMA_SEP + HashtagTable.COLUMN_NAME_TASK_ID + INT
                + COMMA_SEP + HashtagTable.COLUMN_NAME_ARCHIVED + TINY_INT
                + COMMA_SEP + "FOREIGN KEY (" + HashtagTable.COLUMN_NAME_TASK_ID + ") REFERENCES "
                + TaskTable.TABLE_NAME + "(" + TaskTable.COLUMN_NAME_ID + ")"
                + " )";

        String TASK_TABLE_CREATE = "CREATE TABLE " + TaskTable.TABLE_NAME
                + " (" + TaskTable.COLUMN_NAME_ID + PRIMARY_KEY
                + TaskTable.COLUMN_NAME_TITLE + varchar(48) + COMMA_SEP
                + TaskTable.COLUMN_NAME_DETAILS + varchar(48) + COMMA_SEP
                + TaskTable.COLUMN_NAME_PRIORITY + TINY_INT + COMMA_SEP
                + TaskTable.COLUMN_NAME_DATE_CREATED + TIMESTAMP + COMMA_SEP
                + TaskTable.COLUMN_NAME_DATE_REMINDER + TIMESTAMP + COMMA_SEP
                + TaskTable.COLUMN_NAME_REPETITION_MS + LONG + COMMA_SEP
                + TaskTable.COLUMN_NAME_NOTIFY_BEFORE_REMINDER_MS + LONG
                + COMMA_SEP + TaskTable.COLUMN_NAME_IS_COMPLETE + TINY_INT
                + COMMA_SEP + TaskTable.COLUMN_NAME_ARCHIVED + TINY_INT + " )";

        Log.i(SQL_CREATE, HASHTAG_TABLE_CREATE);
        Log.i(SQL_CREATE, TASK_TABLE_CREATE);

        db.execSQL(HASHTAG_TABLE_CREATE);
        db.execSQL(TASK_TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DELETE_HASHTAG = "DROP TABLE IF EXISTS "
                + HashtagTable.TABLE_NAME;
        String SQL_DELETE_TASK = "DROP TABLE IF EXISTS " + TaskTable.TABLE_NAME;

        Log.i(SQL_DELETE, SQL_DELETE_HASHTAG);
        Log.i(SQL_DELETE, SQL_DELETE_TASK);

        db.execSQL(SQL_DELETE_HASHTAG);
        db.execSQL(SQL_DELETE_TASK);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private String varchar(int size) {
        return VARCHAR + "(" + size + ")";

    }

}

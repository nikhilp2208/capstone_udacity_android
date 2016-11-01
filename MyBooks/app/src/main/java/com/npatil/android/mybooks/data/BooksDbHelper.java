package com.npatil.android.mybooks.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nikhil.p on 31/10/16.
 */

public class BooksDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "books.db";

    public BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BooksContract.BooksEntry.TABLE_NAME + " (" +
                BooksContract.BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BooksContract.BooksEntry.COLUMN_BOOK_ID + " TEXT NOT NULL UNIQUE, " +
                BooksContract.BooksEntry.COLUMN_ISBN10 + " TEXT, " +
                BooksContract.BooksEntry.COLUMN_ISBN13 + " TEXT, " +
                BooksContract.BooksEntry.COLUMN_LIST_ID + " TEXT NOT NULL, " +
                BooksContract.BooksEntry.COLUMN_TITLE + " TEXT, " +
                BooksContract.BooksEntry.COLUMN_SUBTITLE + " TEXT, " +
                BooksContract.BooksEntry.COLUMN_DESCRIPTION + " TEXT, " +
                BooksContract.BooksEntry.COLUMN_COVER_PATH + " TEXT, " +
                BooksContract.BooksEntry.COLUMN_RATING + " INTEGER, " +
                BooksContract.BooksEntry.COLUMN_COMMENT + " TEXT);";
        sqLiteDatabase.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BooksContract.BooksEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
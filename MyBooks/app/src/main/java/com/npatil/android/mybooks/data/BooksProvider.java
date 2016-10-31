package com.npatil.android.mybooks.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by nikhil.p on 01/11/16.
 */

public class BooksProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BooksDbHelper mBooksDbHelper;

    public static final int BOOKS = 100;
    public static final int BOOK_WITH_ID = 101;

    private static final String sBookIdSelection = BooksContract.BooksEntry.TABLE_NAME + "." +
            BooksContract.BooksEntry.COLUMN_BOOK_ID + " = ? ";

    @Override
    public boolean onCreate() {
        mBooksDbHelper = new BooksDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
            {
                retCursor = mBooksDbHelper.getReadableDatabase().query(BooksContract.BooksEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            case BOOK_WITH_ID: {
                String bookId = BooksContract.BooksEntry.getBookIdFromUri(uri);
                retCursor = mBooksDbHelper.getReadableDatabase().query(BooksContract.BooksEntry.TABLE_NAME,projection,sBookIdSelection, new String[] {bookId}, null,null,sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BooksContract.BooksEntry.CONTENT_TYPE;
            case BOOK_WITH_ID:
                return BooksContract.BooksEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mBooksDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case BOOKS: {
                long _id = db.insert(BooksContract.BooksEntry.TABLE_NAME,null, values);
                if (_id > 0)
                    returnUri = BooksContract.BooksEntry.buildBooksUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                Log.d("INSERTED", "Inserted id:"+_id);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mBooksDbHelper.getWritableDatabase();
        int _id;

        switch (sUriMatcher.match(uri)) {
            case BOOKS: {
                _id = sqLiteDatabase.delete(BooksContract.BooksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case BOOK_WITH_ID: {
                String bookId = BooksContract.BooksEntry.getBookIdFromUri(uri);
                _id = sqLiteDatabase.delete(BooksContract.BooksEntry.TABLE_NAME, sBookIdSelection, new String[] {bookId});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return _id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mBooksDbHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case BOOKS: {
                count = sqLiteDatabase.update(BooksContract.BooksEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.PATH_BOOKS, BOOKS);
        uriMatcher.addURI(BooksContract.CONTENT_AUTHORITY, BooksContract.PATH_BOOKS + "/*", BOOK_WITH_ID);

        return uriMatcher;
    }
}

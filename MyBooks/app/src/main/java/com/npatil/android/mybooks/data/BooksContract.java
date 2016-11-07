package com.npatil.android.mybooks.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by nikhil.p on 31/10/16.
 */

public class BooksContract {

    public static final String CONTENT_AUTHORITY = "com.npatil.android.mybooks";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOKS = "my_books";

    public static final class BooksEntry implements BaseColumns {
        public static final String TABLE_NAME = "my_books";

        public static final String COLUMN_BOOK_ID = "book_id";
        public static final String COLUMN_ISBN10 = "isbn10";
        public static final String COLUMN_ISBN13 = "isbn13";
        public static final String COLUMN_LIST_ID = "list_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBTITLE = "subtitle";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_COVER_PATH = "cover_path";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_AUTHORS = "authors";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static Uri buildBooksUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildBooksUriWithBookId(String bookId) {
            return CONTENT_URI.buildUpon().appendPath(bookId).build();
        }

        public static String getBookIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }


}

package com.npatil.android.mybooks.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.npatil.android.mybooks.R;
import com.npatil.android.mybooks.data.BooksContract;

/**
 * Created by nikhil.p on 06/11/16.
 */

public class BookWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        BooksContract.BooksEntry.CONTENT_URI,
                        new String[]{
                                BooksContract.BooksEntry._ID,
                                BooksContract.BooksEntry.COLUMN_TITLE
                        },
                        BooksContract.BooksEntry.COLUMN_LIST_ID + " = ?",
                        new String[]{"Reading Now"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                views.setTextViewText(R.id.widget_book_title, data.getString(
                        data.getColumnIndex(BooksContract.BooksEntry.COLUMN_TITLE)));

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getResources().getString(R.string.intent_book_id_string),
                        data.getString(data.getColumnIndex(BooksContract.BooksEntry._ID)));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data != null && data.moveToPosition(position)) {
                    final int BOOKS_ID_COL = 0;
                    return data.getLong(BOOKS_ID_COL);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

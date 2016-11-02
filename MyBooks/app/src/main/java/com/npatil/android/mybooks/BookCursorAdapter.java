package com.npatil.android.mybooks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.npatil.android.mybooks.data.BooksContract;
import com.squareup.picasso.Picasso;

/**
 * Created by nikhil.p on 02/11/16.
 */

public class BookCursorAdapter extends CursorRecyclerViewAdapter<BookCursorAdapter.ViewHolder> {

    private static Context mContext;

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_TITLE));
        viewHolder.title.setText((title!=null?title:"Title NA"));
        //TODO Change this author
        String author = cursor.getString(cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_SUBTITLE));
        viewHolder.author.setText((author!=null?author:"Author NA"));
        Picasso.with(mContext).load(cursor.getString(cursor.getColumnIndex(BooksContract.BooksEntry.COLUMN_COVER_PATH))).into(viewHolder.thumbnail);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView thumbnail;
        public final TextView title;
        public final TextView author;
        public ViewHolder(View itemView){
            super(itemView);
            itemView.setFocusable(true);
            itemView.setClickable(true);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.book_title);
            author = (TextView) itemView.findViewById(R.id.book_author);
        }
    }
}
